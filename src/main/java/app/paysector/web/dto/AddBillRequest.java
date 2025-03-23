package app.paysector.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import app.paysector.bill.model.BillType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddBillRequest {

    @Size(min = 5, max = 20, message = "Bill number length must be between 5 and 20 digits.")
    @NotNull
    private String billNumber;

    @NotNull
    private BillType billType;
}
