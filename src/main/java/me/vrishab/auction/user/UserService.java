package me.vrishab.auction.user;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public User findByUsername(String username) {
        return userRepo.findByEmail(username).
                orElseThrow(() -> new UserNotFoundException(username));
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public User save(User newUser) {
        String email = newUser.getEmail();
        boolean exists = this.userRepo.existsByEmail(email);

        if (!exists) return this.userRepo.save(newUser);

        throw new UserEmailAlreadyExistException(email);
    }

    public User update(String userId, User update) {
        return this.userRepo.findById(UUID.fromString(userId))
                .map(oldUser -> {
                    oldUser.setName(update.getName());
                    oldUser.setPassword(update.getPassword());
                    oldUser.setDescription(update.getDescription());
                    oldUser.setEmail(update.getEmail());
                    oldUser.setContact(update.getContact());
                    oldUser.setEnabled(update.getEnabled());
                    return this.userRepo.save(oldUser);
                })
                .orElseThrow(() -> new UserNotFoundException(UUID.fromString(userId)));
    }

    public void delete(String userId) {
        this.userRepo.findById(UUID.fromString(userId)).orElseThrow(() -> new UserNotFoundException(userId));
        this.userRepo.deleteById(UUID.fromString(userId));
    }
}
