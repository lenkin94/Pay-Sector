package app.paysector.web;

import app.paysector.bill.dto.Bill;
import app.paysector.bill.dto.BillType;
import app.paysector.loan.model.Loan;
import app.paysector.loan.model.LoanStatus;
import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.user.model.Country;
import app.paysector.user.model.User;
import app.paysector.user.model.UserRole;
import app.paysector.wallet.model.Wallet;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

@UtilityClass
public class TestBuilder {
    public static User randomUser() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("solakov")
                .password("asdasd")
                .role(UserRole.USER)
                .country(Country.BULGARIA)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .balance(BigDecimal.ZERO)
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        user.setWallet(wallet);

        return user;
    }

    public static Loan randomLoan() {


        return Loan.builder()
                .id(UUID.fromString("1d7fff1d-ac6c-43c7-8c9f-6c2cb10cca76"))
                .owner(randomUser())
                .amount(BigDecimal.valueOf(1000))
                .interest(0.10)
                .startPeriod(LocalDateTime.now())
                .periodInMonths(3)
                .endPeriod(LocalDateTime.now().plusMonths(3))
                .paymentDate(LocalDate.now().plusDays(30))
                .monthlyPayment(BigDecimal.valueOf(1000))
                .finalSum(BigDecimal.valueOf(1100))
                .amountUntilFUllRepayment(BigDecimal.valueOf(1100))
                .loanStatus(LoanStatus.ACTIVE)
                .requestPayment(false)
                .lastPaymentDate(null)
                .overdueAmount(BigDecimal.ZERO)
                .build();
    }

    public static Wallet randomWallet() {
        return Wallet.builder()
                .id(UUID.randomUUID())
                .owner(randomUser())
                .balance(BigDecimal.valueOf(1000))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
    }

    public static Transaction randomTransaction() {
        return Transaction.builder()
                .owner(randomUser())
                .sender(randomUser().getUsername())
                .receiver("PS")
                .amount(BigDecimal.valueOf(1000))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .type(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCEEDED)
                .balanceLeft(randomUser().getWallet().getBalance())
                .description("asd")
                .failureReason(null)
                .build();
    }

    public static Bill randomBill() {
        return Bill.builder()
                .id(UUID.randomUUID())
                .billNumber("1321231231")
                .billType(BillType.ELECTRICITY)
                .amount(BigDecimal.valueOf(100))
                .startPeriod(LocalDate.now())
                .endPeriod(LocalDate.now())
                .isPaid(false)
                .paidOn(null)
                .build();
    }
}
