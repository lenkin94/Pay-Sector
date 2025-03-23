package app.paysector.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import app.paysector.user.model.Country;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @Size(min = 3, max = 30, message = "Username length must be between 3 and 30 characters!")
    @NotNull
    private String username;

    @Size(min = 3, max = 30, message = "Password length must be between 3 and 30 characters!")
    @NotNull
    private String password;

    @Size(min = 3, max = 30, message = "Password length must be between 3 and 30 characters!")
    @NotNull
    private String confirmPassword;

    @Size(min = 2, max = 20, message = "First name length must be between 2 and 20 characters!")
    @NotNull
    private String firstName;

    @Size(min = 2, max = 20, message = "Last name length must be between 2 and 20 characters!")
    @NotNull
    private String lastName;

    @Email
    @NotNull
    private String email;

    @NotNull
    private Country country;
}
