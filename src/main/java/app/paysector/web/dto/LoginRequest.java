package app.paysector.web.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

    @Size(min = 3, max = 30, message = "Username length must be between 3 and 30 characters!")
    private String username;

    @Size(min = 3, max = 30, message = "Password length must be between 3 and 30 characters!")
    private String password;
}
