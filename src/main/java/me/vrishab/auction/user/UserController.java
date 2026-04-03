package me.vrishab.auction.user;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.auction.converter.AuctionToAuctionDTOConverter;
import me.vrishab.auction.auction.dto.AuctionDTO;
import me.vrishab.auction.item.converter.ItemToAuctionItemDTO;
import me.vrishab.auction.item.dto.AuctionItemDTO;
import me.vrishab.auction.security.AuthService;
import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.converter.BillingDetailsToBillingDetailsDTOConverter;
import me.vrishab.auction.user.converter.UserEditableToUserConverter;
import me.vrishab.auction.user.converter.UserToUserDTOConverter;
import me.vrishab.auction.user.converter.UserUpdateDTOToUserConverter;
import me.vrishab.auction.user.dto.*;
import me.vrishab.auction.user.model.BillingDetails;
import me.vrishab.auction.user.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}")
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserToUserDTOConverter userToUserDTOConverter;
    private final UserEditableToUserConverter userEditableToUserConverter;
    private final UserUpdateDTOToUserConverter userUpdateDTOToUserConverter;
    private final AuctionToAuctionDTOConverter auctionToAuctionDTOConverter;
    private final ItemToAuctionItemDTO itemToAuctionItemDTO;
    private final BillingDetailsToBillingDetailsDTOConverter billingDetailsToBillingDetailsDTOConverter;
    private final AuthService authService;

    public UserController(UserService userService, UserToUserDTOConverter userToUserDTOConverter, UserEditableToUserConverter userEditableToUserConverter, UserUpdateDTOToUserConverter userUpdateDTOToUserConverter, AuctionToAuctionDTOConverter auctionToAuctionDTOConverter, ItemToAuctionItemDTO itemToAuctionItemDTO, BillingDetailsToBillingDetailsDTOConverter billingDetailsToBillingDetailsDTOConverter, AuthService authService) {
        this.userService = userService;
        this.userToUserDTOConverter = userToUserDTOConverter;
        this.userEditableToUserConverter = userEditableToUserConverter;
        this.userUpdateDTOToUserConverter = userUpdateDTOToUserConverter;
        this.auctionToAuctionDTOConverter = auctionToAuctionDTOConverter;
        this.itemToAuctionItemDTO = itemToAuctionItemDTO;
        this.billingDetailsToBillingDetailsDTOConverter = billingDetailsToBillingDetailsDTOConverter;
        this.authService = authService;
    }

    @GetMapping("/users/{username}")
    public Result findUserByUsername(@PathVariable String username) {
        User user = this.userService.findByUsername(username);
        UserDTO userDTO = this.userToUserDTOConverter.convert(user);
        return new Result(true, "Find a user", userDTO);
    }

    @GetMapping("/users")
    public Result findAllUser() {
        List<UserDTO> userDTOS = this.userService.findAll().
                stream().map(userToUserDTOConverter::convert).toList();
        return new Result(true, "Find all users", userDTOS);
    }

    @PostMapping("/users")
    public Result addUser(@Valid @RequestBody UserEditableDTO newUserDTO) {
        User newUser = this.userEditableToUserConverter.convert(newUserDTO);
        User savedUser = this.userService.save(newUser);
        UserDTO savedUserDto = this.userToUserDTOConverter.convert(savedUser);
        return new Result(true, "Add a user", savedUserDto);
    }

    @PutMapping("/user/self")
    public Result updateUser(Authentication auth, @Valid @RequestBody UserUpdateDTO updateDTO) {
        String userId = authService.getUserInfo(auth);

        User update = this.userUpdateDTOToUserConverter.convert(updateDTO);
        User updatedUser = this.userService.update(userId, update);
        UserDTO updatedUserDto = this.userToUserDTOConverter.convert(updatedUser);

        return new Result(true, "Update a user", updatedUserDto);
    }

    @PutMapping("/user/self/password")
    public Result changePassword(Authentication auth, @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        String userId = authService.getUserInfo(auth);

        this.userService.changePassword(userId, changePasswordDTO.currentPassword(), changePasswordDTO.newPassword());

        return new Result(true, "Change password");
    }

    @DeleteMapping("/user/self")
    public Result deleteUser(Authentication auth) {
        String userId = authService.getUserInfo(auth);
        this.userService.delete(userId);

        return new Result(true, "Delete a user");
    }

    @GetMapping("/user/self/wishlist")
    public Result getWishlist(Authentication auth) {
        String userId = authService.getUserInfo(auth);
        List<AuctionItemDTO> itemDTOS = this.userService.wishlist(userId).stream()
                .map(this.itemToAuctionItemDTO::convert).toList();
        return new Result(true, "Get User Wishlist", itemDTOS);
    }

    @PutMapping("/user/self/wishlist/{itemId}")
    public Result addItem(Authentication auth, @PathVariable String itemId) {
        String userId = authService.getUserInfo(auth);
        List<AuctionItemDTO> itemDTOS = this.userService.addItem(userId, itemId).stream()
                .map(this.itemToAuctionItemDTO::convert).toList();
        return new Result(true, "Add Item to Wishlist", itemDTOS);
    }

    @DeleteMapping("/user/self/wishlist/{itemId}")
    public Result removeItem(Authentication auth, @PathVariable String itemId) {
        String userId = authService.getUserInfo(auth);
        List<AuctionItemDTO> itemDTOS = this.userService.removeItem(userId, itemId).stream()
                .map(this.itemToAuctionItemDTO::convert).toList();
        return new Result(true, "Delete Item from Wishlist", itemDTOS);
    }

    @GetMapping("/user/self/auctions")
    public Result getAuctionList(Authentication auth) {
        String userId = authService.getUserInfo(auth);
        List<AuctionDTO> auctionDTOS = this.userService.auctions(userId).stream()
                .map(this.auctionToAuctionDTOConverter::convert).toList();
        return new Result(true, "Get User Auctions", auctionDTOS);
    }

    @PutMapping("/user/self/billingDetails")
    public Result addBillingDetails(Authentication auth, @RequestBody @Valid BillingDetails billingDetails) {
        String userId = authService.getUserInfo(auth);
        this.userService.addBillingDetails(userId, billingDetails);
        return new Result(true, "Add Billing Details");
    }

    @DeleteMapping("/user/self/billingDetails/{id}")
    public Result deleteBillingDetails(Authentication auth, @PathVariable String id) {
        String userId = authService.getUserInfo(auth);
        this.userService.removeBillingDetails(userId, id);
        return new Result(true, "Delete Billing Details");
    }

    @GetMapping("/user/self/billingDetails")
    public Result getBillingDetails(Authentication auth) {
        String userId = authService.getUserInfo(auth);

        List<BillingDetailsDTO> billingDetailsDTOS = this.userService.getBillingDetails(userId)
                .stream().map(this.billingDetailsToBillingDetailsDTOConverter::convert).toList();

        return new Result(true, "Get Billing Details", billingDetailsDTOS);
    }
}
