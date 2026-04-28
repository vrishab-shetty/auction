package me.vrishab.auction.user;

import jakarta.transaction.Transactional;
import me.vrishab.auction.auction.Auction;
import me.vrishab.auction.auction.AuctionRepository;
import me.vrishab.auction.auction.AuctionStatus;
import me.vrishab.auction.item.Item;
import me.vrishab.auction.item.ItemException.ItemNotFoundByIdException;
import me.vrishab.auction.item.ItemRepository;
import me.vrishab.auction.user.UserException.UserEmailAlreadyExistException;
import me.vrishab.auction.user.UserException.UserHasActiveAuctionsException;
import me.vrishab.auction.user.UserException.UserNotFoundByIdException;
import me.vrishab.auction.user.UserException.UserNotFoundByUsernameException;
import me.vrishab.auction.user.UserException.BillingDetailsNotFoundByIdException;
import me.vrishab.auction.user.UserException.UnauthorizedBillingDetailsAccessException;
import me.vrishab.auction.user.model.Address;
import me.vrishab.auction.user.model.BankAccount;
import me.vrishab.auction.user.model.BillingDetails;
import me.vrishab.auction.user.model.CreditCard;
import me.vrishab.auction.user.model.User;
import me.vrishab.auction.wishlist.Wishlist;
import me.vrishab.auction.wishlist.WishlistRepository;
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

    private static final String ANONYMIZED_NAME = "Deleted User";
    private static final String ANONYMIZED_PLACEHOLDER = "N/A";
    private static final String DELETED_EMAIL_DOMAIN = "@auction.app";

    private final UserRepository userRepo;
    private final ItemRepository itemRepo;
    private final AuctionRepository auctionRepo;
    private final WishlistRepository wishlistRepo;

    private final PasswordEncoder passwordEncoder;

    private final BillingDetailsRepository<BillingDetails, UUID> billingDetailsRepo;
    private final CreditCardRepository creditCardRepo;
    private final BankAccountRepository bankAccountRepo;

    public UserService(UserRepository userRepo, ItemRepository itemRepo, AuctionRepository auctionRepo,
                       WishlistRepository wishlistRepo, PasswordEncoder passwordEncoder,
                       BillingDetailsRepository<BillingDetails, UUID> billingDetailsRepo,
                       CreditCardRepository creditCardRepo, BankAccountRepository bankAccountRepo) {
        this.userRepo = userRepo;
        this.itemRepo = itemRepo;
        this.auctionRepo = auctionRepo;
        this.wishlistRepo = wishlistRepo;
        this.passwordEncoder = passwordEncoder;
        this.billingDetailsRepo = billingDetailsRepo;
        this.creditCardRepo = creditCardRepo;
        this.bankAccountRepo = bankAccountRepo;
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
                    oldUser.setName(update.getName());
                    oldUser.setDescription(update.getDescription());
                    oldUser.setContact(update.getContact());
                    oldUser.setEnabled(update.getEnabled());
                    oldUser.setHomeAddress(update.getHomeAddress());
                    return this.userRepo.save(oldUser);
                })
                .orElseThrow(() -> new UserNotFoundByIdException(id));
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        UUID id = UUID.fromString(userId);
        User user = this.userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundByIdException(id));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserException.IncorrectPasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        this.userRepo.save(user);
    }

    public void delete(String userId) {
        User user = getUser(userId);

        // Block deletion while the user has open obligations to bidders.
        boolean hasOpenAuctions = this.auctionRepo.findByUser(user).stream()
                .map(Auction::getStatus)
                .anyMatch(status -> status == AuctionStatus.ACTIVE || status == AuctionStatus.SCHEDULED);
        if (hasOpenAuctions) {
            throw new UserHasActiveAuctionsException(user.getId());
        }

        // Personal data and financial details: hard delete.
        this.wishlistRepo.deleteByUser(user);
        // Other users' wishlist entries pointing at this seller's items are pruned;
        // the items themselves persist so won-item history stays intact for buyers.
        this.wishlistRepo.deleteByItemSeller(user);
        this.billingDetailsRepo.deleteByUser(user);

        // Auctions and Items are retained; the User row is anonymized in place.
        anonymize(user);
        this.userRepo.save(user);
    }

    private void anonymize(User user) {
        user.setEnabled(false);
        user.setName(ANONYMIZED_NAME);
        user.setDescription(null);
        user.setContact(null);
        user.setPassword(null);
        user.setEmail("deleted-" + UUID.randomUUID() + DELETED_EMAIL_DOMAIN);
        scrubAddress(user.getHomeAddress());
    }

    private void scrubAddress(Address address) {
        if (address == null) return;
        // Zipcode and country left intact: a single zip/country pair is low-risk PII
        // and avoids fighting Zipcode validation with placeholder values.
        address.setStreet(ANONYMIZED_PLACEHOLDER);
        address.setCity(ANONYMIZED_PLACEHOLDER);
    }

    public List<Item> wishlist(String userId) {
        User user = getUser(userId);
        return wishlistRepo.findItemsByUser(user);
    }

    public List<Item> addItem(String userId, String itemId) {
        User user = getUser(userId);
        UUID itemUUID = UUID.fromString(itemId);
        Item item = this.itemRepo.findById(itemUUID).orElseThrow(() -> new ItemNotFoundByIdException(itemUUID));
        wishlistRepo.save(new Wishlist(user, item));
        return wishlistRepo.findItemsByUser(user);
    }

    public List<Item> removeItem(String userId, String itemId) {
        User user = getUser(userId);
        UUID itemUUID = UUID.fromString(itemId);
        Item item = this.itemRepo.findById(itemUUID).orElseThrow(() -> new ItemNotFoundByIdException(itemUUID));
        wishlistRepo.deleteByUserAndItem(user, item);
        return wishlistRepo.findItemsByUser(user);
    }

    private User getUser(String userId) {
        UUID userUUID = UUID.fromString(userId);
        return this.userRepo.findById(userUUID).orElseThrow(() -> new UserNotFoundByIdException(userUUID));
    }

    public List<Auction> auctions(String userId) {
        User user = getUser(userId);

        return this.auctionRepo.findByUser(user);
    }

    public void addBillingDetails(String userId, BillingDetails billingDetails) {
        User user = getUser(userId);
        billingDetails.setUser(user);

        if (billingDetails instanceof CreditCard) {
            creditCardRepo.save((CreditCard) billingDetails);
        } else if (billingDetails instanceof BankAccount) {
            bankAccountRepo.save((BankAccount) billingDetails);
        } else {
            throw new IllegalArgumentException("Unknown billing details type");
        }
    }

    public void removeBillingDetails(String userId, String billingDetailsId) {
        User user = getUser(userId);
        UUID billingId = UUID.fromString(billingDetailsId);
        BillingDetails billingDetails = this.billingDetailsRepo.findById(billingId)
                .orElseThrow(() -> new BillingDetailsNotFoundByIdException(billingId));

        if (!billingDetails.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedBillingDetailsAccessException();
        }

        this.billingDetailsRepo.delete(billingDetails);
    }

    public List<BillingDetails> getBillingDetails(String userId) {
        User user = getUser(userId);

        return this.billingDetailsRepo.findByUser(user);
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
