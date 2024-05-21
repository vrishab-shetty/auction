package me.vrishab.auction.user;

import org.springframework.stereotype.Service;

import java.util.List;

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

        if(!exists) return this.userRepo.save(newUser);

        throw new UserEmailAlreadyExistException(email);
    }
}
