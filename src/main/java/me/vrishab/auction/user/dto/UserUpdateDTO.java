package me.vrishab.auction.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

public record UserUpdateDTO(
        @NotBlank(message = "name is required")
        @Size(
                min = 2, max = 25,
                message = "minimum 2 character and maximum 25 characters."
        )
        String name,
        @NotNull
        @Size(
                min = 2, max = 255,
                message = "minimum 2 character and maximum 255 characters."
        )
        String description,
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
