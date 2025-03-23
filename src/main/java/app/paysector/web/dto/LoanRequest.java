package app.paysector.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequest {
    @Min(3)
    @Max(60)
    @NotNull(message = "You must type a loan period!")
    private int periodInMonths;

    @Min(1000)
    @Max(100000)
    @NotNull(message = "You must type amount needed!")
    private BigDecimal amount;
}
