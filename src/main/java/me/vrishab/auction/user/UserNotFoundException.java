package me.vrishab.auction.user;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("Could find user with username " + username);
    }

    public UserNotFoundException(UUID userId) {
        super("Could find user with Id " + userId.toString());
    }
}
