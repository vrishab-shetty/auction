package me.vrishab.auction.system.exception;

import me.vrishab.auction.item.ItemNotFoundException;
import me.vrishab.auction.system.Result;
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
}
