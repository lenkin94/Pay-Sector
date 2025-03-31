package app.paysector.bill.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Bill {

    private UUID id;

    private BillType billType;

    private String billNumber;

    private BigDecimal amount;

    private LocalDate startPeriod;

    private LocalDate endPeriod;

    private boolean isPaid;

    private LocalDate paidOn;
}

