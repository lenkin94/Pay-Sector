package app.paysector;

import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.repository.WalletRepository;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.AddFundsRequest;
import app.paysector.web.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceUTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionService transactionService;
    @Mock
    private UserService userService;

    @InjectMocks
    private WalletService walletService;


    @Test
    void createWallet() {

        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();

        //When
        Wallet wallet = walletService.createWallet(user);

        //Then
        assertThat(wallet.getOwner().getId()).isEqualTo(user.getId());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    void addFunds() {

        //Given
        UUID userId = UUID.randomUUID();
        User user = User.builder().build();
        UUID walletId = UUID.randomUUID();
        Wallet wallet = Wallet.builder()
                .id(walletId)
                .balance(BigDecimal.ZERO)
                .currency(Currency.getInstance("EUR"))
                .owner(user)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        AddFundsRequest request = AddFundsRequest.builder()
                .cardNumber("1231231231231231")
                .expireDate(LocalDate.now())
                .cvv("123")
                .amount(BigDecimal.valueOf(1000))
                .ownerName("Borislav Solakov")
                .build();

        //When
        walletService.addFunds(wallet, request);

        //Then
        assertThat(wallet.getBalance()).isEqualTo(request.getAmount());
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    void nonExistingUsername_whenTransferFunds_doesNotAffectSenderWallet() {

        //Given
        UUID userId = UUID.randomUUID();
        Wallet senderWallet = Wallet.builder()
                .balance(BigDecimal.valueOf(1000))
                .currency(Currency.getInstance("EUR"))
                .build();

        User sender = User.builder()
                .username("lenkin")
                .id(userId)
                .wallet(senderWallet)
                .build();

        TransferRequest request = TransferRequest.builder()
                .receiverUsername("asd")
                .amount(BigDecimal.valueOf(1000))
                .transferDescription("")
                .build();

        when(walletRepository.findByOwnerId(userId)).thenReturn(senderWallet);
        when(walletRepository.findByOwnerUsername(request.getReceiverUsername())).thenReturn(Optional.empty());

        //When
        walletService.transferFunds(sender, request);


        //Then
        assertThat(senderWallet.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        verify(walletRepository, never()).save(any());
    }

    @Test
    void notEnoughBalance_whenTransferFunds_doesNotAffectSenderWallet() {

        //Given
        UUID userId = UUID.randomUUID();
        Wallet senderWallet = Wallet.builder()
                .balance(BigDecimal.valueOf(100))
                .currency(Currency.getInstance("EUR"))
                .build();

        User sender = User.builder()
                .username("lenkin")
                .id(userId)
                .wallet(senderWallet)
                .build();

        TransferRequest request = TransferRequest.builder()
                .receiverUsername("asd")
                .amount(BigDecimal.valueOf(1000))
                .transferDescription("")
                .build();

        when(walletRepository.findByOwnerId(userId)).thenReturn(senderWallet);
        when(walletRepository.findByOwnerUsername(request.getReceiverUsername())).thenReturn(Optional.empty());

        //When
        walletService.transferFunds(sender, request);

        //Then
        assertThat(senderWallet.getBalance()).isEqualTo(BigDecimal.valueOf(100));
        verify(walletRepository, never()).save(any());
    }

    @Test
    void successfulTransferWithoutDescription_whenTransferFunds_transferFunds_success() {

        //Given
        UUID senderId = UUID.randomUUID();
        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(1000))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        User sender = User.builder()
                .username("lenkin")
                .id(senderId)
                .wallet(senderWallet)
                .build();

        User receiver = User.builder()
                .username("solakov")
                .id(senderId)
                .build();
        Wallet receiverWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(0))
                .currency(Currency.getInstance("EUR"))
                .owner(receiver)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        TransferRequest request = TransferRequest.builder()
                .receiverUsername("asd")
                .amount(BigDecimal.valueOf(1000))
                .transferDescription("")
                .build();

        when(walletRepository.findByOwnerId(senderId)).thenReturn(senderWallet);
        when(walletRepository.findByOwnerUsername(request.getReceiverUsername())).thenReturn(Optional.of(receiverWallet));

        //When
        walletService.transferFunds(sender, request);

        //Then
        assertThat(senderWallet.getBalance()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(receiverWallet.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        verify(walletRepository, times(2)).save(any());
    }

    @Test
    void successfulTransferWithDescription_whenTransferFunds_transferFunds_success() {

        //Given
        UUID senderId = UUID.randomUUID();
        Wallet senderWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(1000))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();
        User sender = User.builder()
                .username("lenkin")
                .id(senderId)
                .wallet(senderWallet)
                .build();

        User receiver = User.builder()
                .username("solakov")
                .id(senderId)
                .build();
        Wallet receiverWallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(BigDecimal.valueOf(0))
                .currency(Currency.getInstance("EUR"))
                .owner(receiver)
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

        TransferRequest request = TransferRequest.builder()
                .receiverUsername("asd")
                .amount(BigDecimal.valueOf(1000))
                .transferDescription("zemi")
                .build();

        when(walletRepository.findByOwnerId(senderId)).thenReturn(senderWallet);
        when(walletRepository.findByOwnerUsername(request.getReceiverUsername())).thenReturn(Optional.of(receiverWallet));

        //When
        walletService.transferFunds(sender, request);

        //Then
        assertThat(senderWallet.getBalance()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(receiverWallet.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
        verify(walletRepository, times(2)).save(any());
    }



    @Test
    void updateWalletWithdrawal() {
        BigDecimal amount = BigDecimal.valueOf(100);
        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.valueOf(100))
                .updatedOn(LocalDateTime.now())
                .build();

        walletService.updateWalletWithdrawal(wallet, amount);

        assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(0));
        verify(walletRepository, times(1)).save(any());
    }

    @Test
    void updateWalletDeposit() {
        BigDecimal amount = BigDecimal.valueOf(100);
        Wallet wallet = Wallet.builder()
                .balance(BigDecimal.valueOf(0))
                .updatedOn(LocalDateTime.now())
                .build();

        walletService.updateWalletDeposit(wallet, amount);

        assertThat(wallet.getBalance()).isEqualTo(BigDecimal.valueOf(100));
        verify(walletRepository, times(1)).save(any());
    }
}
