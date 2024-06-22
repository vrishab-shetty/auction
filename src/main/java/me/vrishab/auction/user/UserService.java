package me.vrishab.auction.user;

import jakarta.transaction.Transactional;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemException.ItemNotFoundByIdException;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.user.UserException.UserEmailAlreadyExistException;
import me.vrishab.auction.user.UserException.UserNotFoundByIdException;
import me.vrishab.auction.user.UserException.UserNotFoundByUsernameException;
import me.vrishab.auction.user.model.BankAccount;
import me.vrishab.auction.user.model.BillingDetails;
import me.vrishab.auction.user.model.CreditCard;
import me.vrishab.auction.user.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final PasswordEncoder passwordEncoder;
    private final CreditCardRepository creditCardRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserService(UserRepository userRepo, ItemRepository itemRepo, PasswordEncoder passwordEncoder, CreditCardRepository creditCardRepository, BankAccountRepository bankAccountRepository) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.passwordEncoder = passwordEncoder;
        this.creditCardRepository = creditCardRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public User findByUsername(String username) {
        return userRepo.findByEmail(username).
                orElseThrow(() -> new UserNotFoundByUsernameException(username));
    }

    public List<User> findAll() {
        return userRepo.findAll();
    }

    public User save(User newUser) {
        checkEmail(newUser.getEmail());

        newUser.setPassword(this.passwordEncoder.encode(newUser.getPassword()));
        return this.userRepo.save(newUser);
    }

    public User update(String userId, User update) {
        UUID id = UUID.fromString(userId);
        return this.userRepo.findById(id)
                .map(oldUser -> {

                    if (!oldUser.getEmail().equals(update.getEmail())) {
                        checkEmail(update.getEmail());
                    }

                    oldUser.setName(update.getName());
                    oldUser.setPassword(this.passwordEncoder.encode(update.getPassword()));
                    oldUser.setDescription(update.getDescription());
                    oldUser.setEmail(update.getEmail());
                    oldUser.setContact(update.getContact());
                    oldUser.setEnabled(update.getEnabled());
                    oldUser.setHomeAddress(update.getHomeAddress());
                    return this.userRepo.save(oldUser);
                })
                .orElseThrow(() -> new UserNotFoundByIdException(id));
    }

    public void delete(String userId) {
        UUID id = UUID.fromString(userId);
        this.userRepo.findById(id).orElseThrow(() -> new UserNotFoundByIdException(id));
        this.userRepo.deleteById(id);
    }

    public List<Item> wishlist(String userId) {
        User user = getUser(userId);

        return user.getWishlist().stream().toList();
    }


    public List<Item> addItem(String userId, String itemId) {
        User user = getUser(userId);
        UUID itemUUID = UUID.fromString(itemId);
        Item item = this.itemRepo.findById(itemUUID).orElseThrow(() -> new ItemNotFoundByIdException(itemUUID));
        user.addFavouriteItem(item);
        return this.userRepo.save(user).getWishlist()
                .stream().toList();
    }

    public List<Item> removeItem(String userId, String itemId) {
        User user = getUser(userId);
        UUID itemUUID = UUID.fromString(itemId);
        Item item = this.itemRepo.findById(itemUUID).orElseThrow(() -> new ItemNotFoundByIdException(itemUUID));
        user.removeFavouriteItem(item);
        return this.userRepo.save(user).getWishlist()
                .stream().toList();
    }

    private User getUser(String userId) {
        UUID userUUID = UUID.fromString(userId);
        return this.userRepo.findById(userUUID).orElseThrow(() -> new UserNotFoundByIdException(userUUID));
    }

    public List<Auction> auctions(String userId) {
        User user = getUser(userId);

        return user.getAuctions().stream().toList();
    }

    public void addBillingDetails(String userId, BillingDetails billingDetails) {
        User user = getUser(userId);
        user.addBillingDetail(billingDetails);

        if (billingDetails instanceof CreditCard) {
            creditCardRepository.save((CreditCard) billingDetails);
        } else if (billingDetails instanceof BankAccount) {
            bankAccountRepository.save((BankAccount) billingDetails);
        } else {
            throw new IllegalArgumentException("Unknown billing details type");
        }
    }

    public Set<BillingDetails> getBillingDetails(String userId) {
        User user = getUser(userId);
        return user.getBillingDetails();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return this.userRepo.findByEmail(username)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Could find user with username " + username));
    }

    private void checkEmail(String email) {

        boolean exists = this.userRepo.existsByEmail(email);

        if (exists) {
            throw new UserEmailAlreadyExistException(email);
        }
    }


}
