# Auction Management System

This is a RESTful API for an auction system built with Spring Boot. It allows users to create and bid on auctions for
various items. The application includes features for user authentication, data validation, and a clear separation of
concerns in its architecture.

## Technologies Used

* **Java 17**: The core programming language.
* **Spring Boot 3.2.5**: The application framework.
* **Spring Web**: For building RESTful APIs.
* **Spring Data JPA**: For data persistence.
* **Spring Security**: For authentication and authorization.
* **PostgreSQL**: Production database.
* **Redis**: Used for caching and distributed locking to handle concurrent bids.
*
    * **Testcontainers**: For integration testing with real database and Redis instances.
* **Lombok**: To reduce boilerplate code.
* **Gradle**: The build tool for the project.

## Configuration & Setup

### 1. Start Infrastructure (Database & Redis)

The application requires PostgreSQL (port 5432) and Redis (port 6379).

You can start them quickly using Docker:

```bash
# Start PostgreSQL
docker run --name auction-db -e POSTGRES_DB=auction -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:latest

# Start Redis
docker run --name auction-redis -p 6379:6379 -d redis:7
```

## How to Build and Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/vrishab-shetty/auction.git
   cd auction
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```
   The application will be available at `http://localhost:8080`.

## API Endpoints

The primary API endpoints are:

* `/users`: For user management.
* `/auctions`: For creating and managing auctions.
* `/items`: For managing the items available for auction.

(More detailed API documentation to be added)

# Docker

To build the Docker image for this project, use the following command:

```bash
docker build -t vrishab.me/spring/auction:1.0 .
```

# GCloud

## To add the local image to the GCloud Artifact Registry

1) Authenticate:

        gcloud auth configure-docker HOSTNAME-LIST (us-east1-docker.pkg.dev, gcr.io, etc)

   For example, gcloud auth configure-docker us-east1-docker.pkg.dev,asia-northeast1-docker.pkg.dev

2) Tag the local image with the repository name:

        docker tag SOURCE-IMAGE HOSTNAME-LIST/PROJECT-ID/REPO-NAME/TARGET-IMAGE:TAG
   For example, docker tag vrishab.me/spring/auction:1.0 us-east1-docker.pkg.dev/cloud-23831/docker/auction

3) Push the tagged image with the command:

        docker push HOSTNAME-LIST/PROJECT-ID/REPO-NAME/TARGET-IMAGE:TAG
   For example, docker push us-east1-docker.pkg.dev/cloud-23831/docker/auction:latest

**Note: If you normally run Docker commands on Linux with sudo, Docker looks for Artifact Registry credentials in
/root/.docker/config.json instead of $HOME/.docker/config.json**
