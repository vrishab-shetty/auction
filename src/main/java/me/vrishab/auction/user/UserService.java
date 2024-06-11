package me.vrishab.auction.user;

import jakarta.transaction.Transactional;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.system.exception.ObjectNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, ItemRepository itemRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
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
                .orElseThrow(() -> new ObjectNotFoundException("user", id));
    }

    public void delete(String userId) {
        UUID id = UUID.fromString(userId);
        this.userRepo.findById(id).orElseThrow(() -> new ObjectNotFoundException("user", id));
        this.userRepo.deleteById(id);
    }

    public List<Item> wishlist(String userId) {
        UUID id = UUID.fromString(userId);
        User user = this.userRepo.findById(id).orElseThrow(() -> new ObjectNotFoundException("user", id));

        return user.getWishlist().stream().toList();
    }


    public List<Item> addItem(String userId, String itemId) {
        UUID userUUID = UUID.fromString(userId);
        UUID itemUUID = UUID.fromString(itemId);
        User user = this.userRepo.findById(userUUID).orElseThrow(() -> new ObjectNotFoundException("user", userUUID));
        Item item = this.itemRepo.findById(itemUUID).orElseThrow(() -> new ObjectNotFoundException("item", itemUUID));
        user.addFavouriteItem(item);
        return this.userRepo.save(user).getWishlist()
                .stream().toList();
    }

    public List<Item> removeItem(String userId, String itemId) {
        UUID userUUID = UUID.fromString(userId);
        UUID itemUUID = UUID.fromString(itemId);
        User user = this.userRepo.findById(userUUID).orElseThrow(() -> new ObjectNotFoundException("user", userUUID));
        Item item = this.itemRepo.findById(itemUUID).orElseThrow(() -> new ObjectNotFoundException("item", itemUUID));
        user.removeFavouriteItem(item);
        return this.userRepo.save(user).getWishlist()
                .stream().toList();
    }

    public List<Auction> auctions(String userId) {
        UUID id = UUID.fromString(userId);
        User user = this.userRepo.findById(id).orElseThrow(() -> new ObjectNotFoundException("user", id));

        return user.getAuctions().stream().toList();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return this.userRepo.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Could find user with username " + username));
    }
}
