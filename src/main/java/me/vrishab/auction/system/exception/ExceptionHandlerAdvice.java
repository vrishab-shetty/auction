package me.vrishab.auction.system.exception;

import me.vrishab.auction.item.ItemBadRequestException;
import me.vrishab.auction.item.ItemNotFoundException;
import me.vrishab.auction.system.Result;
import me.vrishab.auction.user.UserEmailAlreadyExistException;
import me.vrishab.auction.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @ExceptionHandler(UserEmailAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleDuplicateEmailRequest(UserEmailAlreadyExistException ex) {
        return new Result(false, ex.getMessage());
    }
}
