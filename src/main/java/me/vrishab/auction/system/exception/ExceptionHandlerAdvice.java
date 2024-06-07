package me.vrishab.auction.system.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import me.vrishab.auction.auction.AuctionNotFoundException;
import me.vrishab.auction.auction.UnAuthorizedAuctionAccess;
import me.vrishab.auction.item.ItemNotFoundException;
import me.vrishab.auction.security.AuthenticationRequiredException;
import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.UserEmailAlreadyExistException;
import me.vrishab.auction.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.*;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerAdvice {

    // Items
    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleItemNotFoundException(ItemNotFoundException ex) {
        return new Result(false, ex.getMessage());
    }

    // Users
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleItemNotFoundException(UserNotFoundException ex) {
        return new Result(false, ex.getMessage());
    }

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleDuplicateEmailRequest(UserEmailAlreadyExistException ex) {
        return new Result(false, ex.getMessage());
    }

    // Auctions
    @ExceptionHandler(AuctionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleAuctionNotFoundException(AuctionNotFoundException ex) {
        return new Result(false, ex.getMessage());
    }

    @ExceptionHandler(UnAuthorizedAuctionAccess.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleUnauthorizedAuctionAccess(UnAuthorizedAuctionAccess ex) {
        return new Result(false, ex.getMessage());
    }

    // Validations
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String[] parts = violation.getPropertyPath().toString().split("\\.");
            String fieldName = parts[parts.length - 1];  // Get the last part of the path
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }
        return new Result(false, "Provided arguments are invalid, see data for details", errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleValidationException(MethodArgumentNotValidException ex) {
        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        Map<String, String> map = new HashMap<>(errors.size());
        errors.forEach((error) -> {
            String key = ((FieldError) error).getField();
            String val = error.getDefaultMessage();
            map.put(key, val);
        });

        return new Result(false, "Provided arguments are invalid, see data for details", map);
    }

    // Authentication and Authorization
    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleAuthenticationException(Exception ex) {
        return new Result(false, "username or password is incorrect", ex.getMessage());
    }

    @ExceptionHandler(AccountStatusException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleAccountStatusException(AccountStatusException ex) {
        return new Result(false, "User account is disabled", ex.getMessage());
    }

    @ExceptionHandler(InvalidBearerTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleInvalidTokenException(InvalidBearerTokenException ex) {
        return new Result(false, "The access token provided is expired, revoked, or invalid", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleInvalidTokenException(AccessDeniedException ex) {
        return new Result(false, "No permission", ex.getMessage());
    }

    @ExceptionHandler({AuthenticationRequiredException.class, InsufficientAuthenticationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleBasicAuthenticationException(Exception ex) {
        return new Result(false, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Result handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return new Result(false, "A server error occurs:\n" + ex.getMessage(), Arrays.toString(ex.getStackTrace()));
    }


}
