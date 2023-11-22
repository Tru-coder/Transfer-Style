package com.example.transferstylerebuildmaven.requests;

import com.example.transferstylerebuildmaven.models.user.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {


    @NotBlank(message = "Username cannot be empty")
    @Size(min = 4, max = 50, message = "the field must consist of 4 to 50 characters")
    private String username;

    @NotBlank(message = "firstname cannot be empty")
    @Size(min = 2, max = 50, message = "the field must consist of 2 to 50 characters")
    private String firstname;


    @NotBlank(message = "lastname cannot be empty")
    @Size(min = 2, max = 50, message = "the field must consist of 2 to 50 characters")
    private String lastname;


    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Not valid email")
    private String email;


    @NotBlank
    @Size(min = 8, max = 50, message = "the field must consist of 8 to 50 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*?#&]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, " +
                    "one digit, and one special character")
    private String password;

    @NotNull(message = "Role must not be null")
    @NotBlank
    @Pattern(regexp = "^(USER|ADMIN|MANAGER)$", message = "Role must be one of: USER, ADMIN, MANAGER")
    private String role;


}
