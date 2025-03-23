package app.paysector.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddFundsRequest {

    @NotNull
    @Size(min = 16, max = 16, message = "Card number must be 16 digits.")
    private String cardNumber;

    @NotNull
    private String ownerName;

    @NotNull
    private LocalDate expireDate;

    @NotNull
    private String cvv;

    @NotNull
    @Min(10)
    private BigDecimal amount;
}
