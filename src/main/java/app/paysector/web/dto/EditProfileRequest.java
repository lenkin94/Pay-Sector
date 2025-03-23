package app.paysector.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import app.paysector.user.model.Country;
import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EditProfileRequest {
    @Size(min = 3, max = 30, message = "First name length must be between 3 and 30 characters!")
    @NotNull
    private String firstName;

    @Size(min = 3, max = 30, message = "Last name length must be between 3 and 30 characters!")
    @NotNull
    private String lastName;

    @Email(message = "Requires correct email format")
    @NotNull
    private String email;

    @URL(message = "Requires correct web link format")
    private String profilePicture;

    @NotNull
    private Country country;
}
