package app.paysector;

import app.paysector.loan.model.Loan;
import app.paysector.loan.repository.LoanRepository;
import app.paysector.loan.service.LoanService;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.LoanRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanServiceUTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserService userService;
    @Mock
    private WalletService walletService;
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private LoanService loanService;

    @Test
    void createLoan() {

        LoanRequest request = LoanRequest.builder()
                .periodInMonths(3)
                .amount(BigDecimal.valueOf(1000))
                .build();

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .createdAt(LocalDateTime.now())
                .build();

        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .owner(user)
                .balance(BigDecimal.valueOf(0))
                .updatedOn(LocalDateTime.now())
                .currency(Currency.getInstance("EUR"))
                .build();


        loanService.createLoan(user, request, wallet);

        assertThat(user.getLoans().size()).isEqualTo(1);

    }
}
