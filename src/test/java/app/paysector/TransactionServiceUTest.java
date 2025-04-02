package app.paysector;

import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.repository.TransactionRepository;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.wallet.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceUTest {
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction() {

        User user = User.builder()
                .username("lenkin")
                .build();
        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.TEN)
                .build();

        transactionService.createNewTransaction(user, user.getUsername(), "", BigDecimal.valueOf(100), wallet.getBalance(), wallet.getCurrency(), TransactionType.WITHDRAWAL, TransactionStatus.SUCCEEDED, "", null);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void findTransactionByOwnerId() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();

        Transaction transaction = Transaction.builder()
                .owner(user)
                .build();

        List<Transaction> transactions = List.of(transaction);

        when(transactionService.findByOwnerId(userId)).thenReturn(transactions);

        List<Transaction> byOwnerId = transactionService.findByOwnerId(userId);

        assertEquals(transactions, byOwnerId);

    }
}
