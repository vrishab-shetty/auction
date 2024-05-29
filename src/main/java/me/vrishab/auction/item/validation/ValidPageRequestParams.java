package me.vrishab.auction.item.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PageRequestParamsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPageRequestParams {
    String message() default "Both pageNum and pageSize must be provided or none";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
