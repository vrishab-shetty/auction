package me.vrishab.auction.user.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public record UserEditableDTO(
        @NotBlank(message = "name is required")
        @Size(
                min = 2, max = 25,
                message = "minimum 2 character and maximum 25 characters."
        )
        String name,
        @NotBlank(message = "password is required")
        @Size(
                min = 8, max = 64,
                message = "minimum 8 character and maximum 64 characters"
        )
        String password,
        @NotNull
        @Size(
                min = 2, max = 255,
                message = "minimum 2 character and maximum 255 characters."
        )
        String description,
        @NotBlank(message = "email is required")
        @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "Please provide a valid email address")
        String email,
        @NotBlank(message = "contact info is required")
        @Size(min = 10, max = 10, message = "Please provide a valid phone number")
        String contact,

        @NotNull(message = "zipcode is required")
        @Length(min = 4, max = 10, message = "Please provide a valid ZIP code")
        String zipCode,

        @NotNull(message = "street is required")
        String street,

        @NotNull(message = "city is required")
        String city,

        @NotNull(message = "country is required")
        String country
) {
}
