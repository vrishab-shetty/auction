package me.vrishab.auction.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
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

        if (!exists) {
            newUser.setPassword(this.passwordEncoder.encode(newUser.getPassword()));
            return this.userRepo.save(newUser);
        }

        throw new UserEmailAlreadyExistException(email);
    }

    public User update(String userId, User update) {
        UUID id = UUID.fromString(userId);
        return this.userRepo.findById(id)
                .map(oldUser -> {
                    oldUser.setName(update.getName());
                    oldUser.setPassword(this.passwordEncoder.encode(update.getPassword()));
                    oldUser.setDescription(update.getDescription());
                    oldUser.setEmail(update.getEmail());
                    oldUser.setContact(update.getContact());
                    oldUser.setEnabled(update.getEnabled());
                    return this.userRepo.save(oldUser);
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void delete(String userId) {
        UUID id = UUID.fromString(userId);
        this.userRepo.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        this.userRepo.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return this.userRepo.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Could find user with username " + username));
    }
}
