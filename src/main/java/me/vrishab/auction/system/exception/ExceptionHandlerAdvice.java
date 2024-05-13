package me.vrishab.auction.system.exception;

import me.vrishab.auction.item.ItemNotFoundException;
import me.vrishab.auction.system.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(ItemNotFoundException.class)
    public Result handleItemNotFoundException(ItemNotFoundException ex) {
        return new Result(false, ex.getMessage());
    }
}
