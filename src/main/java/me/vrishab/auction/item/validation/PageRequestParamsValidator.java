package me.vrishab.auction.item.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import me.vrishab.auction.system.PageRequestParams;

public class PageRequestParamsValidator implements ConstraintValidator<ValidPageRequestParams, PageRequestParams> {

    @Override
    public void initialize(ValidPageRequestParams constraintAnnotation) {
    }

    @Override
    public boolean isValid(PageRequestParams pageRequestParams, ConstraintValidatorContext context) {
        if (pageRequestParams == null) {
            return true;
        }

        boolean pageNumProvided = pageRequestParams.getPageNum() != null;
        boolean pageSizeProvided = pageRequestParams.getPageSize() != null;

        if (pageNumProvided && pageSizeProvided) {
            return true;
        }

        if (!pageNumProvided && !pageSizeProvided) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        if (!pageNumProvided) {
            context.buildConstraintViolationWithTemplate("pageNum is required when pageSize is provided")
                    .addPropertyNode("pageNum")
                    .addConstraintViolation();
        }
        if (!pageSizeProvided) {
            context.buildConstraintViolationWithTemplate("pageSize is required when pageNum is provided")
                    .addPropertyNode("pageSize")
                    .addConstraintViolation();
        }
        return false;
    }
}
