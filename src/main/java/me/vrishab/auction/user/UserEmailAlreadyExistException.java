package me.vrishab.auction.user;

public class UserEmailAlreadyExistException extends RuntimeException {
    public UserEmailAlreadyExistException(String email) {
        super("The email " + email + " already exist");
    }
}
