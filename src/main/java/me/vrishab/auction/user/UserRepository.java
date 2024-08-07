package me.vrishab.auction.user;

import me.vrishab.auction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, CustomUserRepository {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);
}
