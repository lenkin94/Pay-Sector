package app.paysector.bill.model;

import jakarta.persistence.*;
import lombok.*;
import app.paysector.user.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BillType billType;

    @Column(nullable = false, unique = true)
    private String billNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate startPeriod;

    @Column(nullable = false)
    private LocalDate endPeriod;

    @Column(nullable = false)
    private boolean isPaid;

    private LocalDate payedOn;

    @ManyToOne
    private User owner;
}

