package me.vrishab.auction.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordDTO(
        @NotBlank(message = "current password is required")
        String currentPassword,
        @NotBlank(message = "new password is required")
        @Size(
                min = 8, max = 64,
                message = "minimum 8 character and maximum 64 characters"
        )
        String newPassword
) {
}
