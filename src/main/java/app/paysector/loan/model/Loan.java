package app.paysector.loan.model;

import jakarta.persistence.*;
import lombok.*;
import app.paysector.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int periodInMonths;

    @Column(nullable = false)
    private LocalDateTime startPeriod;

    @Column(nullable = false)
    private LocalDateTime endPeriod;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private double interest;

    @Column(nullable = false)
    private BigDecimal monthlyPayment;

    @Column(nullable = false)
    private BigDecimal amountUntilFUllRepayment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;

    @Column(nullable = false)
    private boolean requestPayment;

    @Column(nullable = false)
    private BigDecimal finalSum;

    private LocalDateTime lastPaymentDate;

    @Column(nullable = false)
    private BigDecimal overdueAmount;

    @ManyToOne
    private User owner;
}
