# Concurrency Mechanisms in Auction Management System

## 1. Overview

**Purpose of introducing concurrency**
In a multi-user distributed system like an online auction platform, concurrent access to shared resources is inevitable. The application has to process multiple overlapping requests for critical operations, particularly placing bids on items. Bidding requires safe, consistent, and sequentially guaranteed state transitions to ensure fairness and correctness. Without explicit concurrency mechanisms, concurrent updates to an auction item's price might result in data inconsistencies or lost updates.

**Problems it solves in the application**
1. **Lost Updates:** Two users might fetch the same item state and simultaneously place bids based on that state, potentially overwriting each other's updates.
2. **Race Conditions in Bid Validation:** When bids are placed concurrently, the bid validation logic (ensuring a new bid is strictly higher than the current bid) could fail if the validation happens on stale data.
3. **Optimistic Overwrites:** The system needs a fallback safety mechanism to reject operations that manage to proceed past preliminary checks but try to commit over already modified data.
4. **Data Consistency:** Caching layers (e.g., Redis ranking of bids) need to stay in sync with the primary persistent data source (PostgreSQL database) when high volumes of bids are applied simultaneously.

## 2. Concurrency Model

**Approach used**
The application adopts a **hybrid concurrency model** that pairs a lightweight distributed lock layer (using Redis) with database-level optimistic locking (using JPA `@Version`).

**High-level design and workflow**
1. **Acquire Distributed Lock (Pessimistic-like guard):** When a user attempts to bid on an item, the application first tries to acquire a lock specific to the item (`lock:item:{itemId}`) in Redis. If it cannot acquire the lock, the thread immediately throws a `ConcurrentBidException`, signaling that the resource is currently under high contention and the request should be aborted or retried by the client.
2. **Execute Database Transaction:** Once the lock is acquired, the application starts a database transaction. It fetches the `Item` state from the database, evaluates the business rules for bidding (e.g., bid amount validity, auction status), and modifies the `Item` entity.
3. **Optimistic Locking Guard (Database-level):** When the application attempts to save the updated `Item` entity, JPA/Hibernate issues an update statement carrying a version check (`UPDATE Item SET ... WHERE id = ? AND version = ?`). If another transaction somehow bypassed the lock or updated the entity concurrently, the version mismatch triggers an `OptimisticLockException`, safely rolling back the operation.
4. **Post-Commit Synchronization:** Upon successful commit of the database transaction, a registered `TransactionSynchronization` updates the external cache (a Redis Sorted Set representing bid scores).
5. **Release Lock:** Finally, regardless of whether the transaction succeeded or failed, the Redis distributed lock is explicitly released via a Lua script.

## 3. Key Components

### Modules/files responsible for concurrency
* **`AuctionService.java` (`bid` method)**: Serves as the primary orchestrator for the concurrent bid workflow. It manages the lifecycle of the Redis lock and defines the transactional boundaries using Spring's `TransactionTemplate`.
* **`Item.java` (`@Version Long version`)**: Represents the persistent entity. The `@Version` annotation delegates the concurrency control down to the ORM and database level.
* **`ConcurrencyIntegrationTest.java`**: Evaluates and asserts the correctness of the concurrency logic, testing scenarios like high contention, lock timeouts, transaction rollbacks, and mutual exclusion.

### Description of their roles and interactions
* **Redis `setIfAbsent` (SETNX)**: Grants mutual exclusion at the application cluster level. This acts as the first line of defense to reject conflicting updates fast, thus avoiding heavy contention and lock thrashing at the database layer.
* **Spring `TransactionTemplate`**: It defines a precise procedural transaction boundary inside the lock. By utilizing `TransactionTemplate` over the `@Transactional` annotation for the `bid` method, the system ensures the transaction is isolated inside the critical section (the lock is acquired *before* the transaction begins and released *after* the transaction is committed or rolled back).
* **Spring `TransactionSynchronizationManager`**: It defers secondary operations (like updating the Redis ZSet for ranking) to the `afterCommit` phase. This avoids race conditions where the cache is updated but the primary transaction rolls back.
* **JPA Optimistic Locking (`@Version`)**: The ultimate safety net guaranteeing ACID properties at the database layer. It increments an entity version on every update, aborting transactions attempting to modify stale data.

## 4. Synchronization & Thread Safety

**How shared resources are handled**
The main shared resource, the `Item` being auctioned, is isolated by unique IDs. The concurrency mechanisms use "fine-grained locking" at the per-item scope rather than locking the entire auction or system. The Redis lock key explicitly targets individual items (`lock:item:{itemId}`), enabling parallel bids across different items simultaneously.

**Strategies used to prevent race conditions and deadlocks**
* **Timeouts on Locks**: The distributed Redis lock is given a strict Time-to-Live (TTL) of 5 seconds (`Duration.ofSeconds(5)`). This prevents deadlock scenarios where a process crashes or hangs while holding the lock.
* **Atomic Lock Release**: The Redis lock is released using a Lua script that checks whether the currently executing thread is the owner of the lock before deleting it. This prevents a slow thread from inadvertently releasing a lock that has expired and been acquired by another thread.
* **Transaction Template vs Annotation**: Starting the transaction *inside* the lock's scope avoids scenarios where a stale entity is fetched at the beginning of the transaction, *then* the lock is acquired, leading to race conditions despite locking.
* **Deferred External Actions**: By binding external side-effects (e.g., updating Redis leaderboards) to `afterCommit` transaction hooks, the system guarantees that external views are only updated once the persistent state change is hardened, maintaining system-wide consistency.
