package app.paysector.bill.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetAllUserBills {
    private List<Bill> bills;
}
