package me.vrishab.auction.security;

public class AuthenticationRequiredException extends RuntimeException {

    public AuthenticationRequiredException(AuthType authType) {
        super(
                (authType == AuthType.BASIC ? "Basic" : "Bearer Token")
                        + " Authentication is required"
        );
    }

    public enum AuthType {
        BASIC, BEARER_TOKEN
    }
}
