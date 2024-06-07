package me.vrishab.auction.user;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Could find user with username " + username);
    }
}
