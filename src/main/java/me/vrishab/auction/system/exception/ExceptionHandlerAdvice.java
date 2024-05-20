package me.vrishab.auction.system.exception;

import me.vrishab.auction.item.ItemBadRequestException;
import me.vrishab.auction.item.ItemNotFoundException;
import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleItemNotFoundException(ItemNotFoundException ex) {
        return new Result(false, ex.getMessage());
    }

    @ExceptionHandler(ItemBadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleItemBadRequestException(ItemBadRequestException ex) {
        return new Result(false, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleItemNotFoundException(UserNotFoundException ex) {
        return new Result(false, ex.getMessage());
    }
}
