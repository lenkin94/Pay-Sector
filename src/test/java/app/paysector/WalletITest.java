package app.paysector;

import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.user.model.Country;
import app.paysector.user.model.User;
import app.paysector.user.repository.UserRepository;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.repository.WalletRepository;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.AddFundsRequest;
import app.paysector.web.dto.RegisterRequest;
import app.paysector.web.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class WalletITest {

    @Autowired
    private UserService userService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired private WalletRepository walletRepository;

    @Test
    void transferFunds() {
        RegisterRequest registerRequestSender = RegisterRequest.builder()
                .username("lenkin")
                .password("asdasd")
                .confirmPassword("asdasd")
                .email("lenkin@mail.bg")
                .country(Country.BULGARIA)
                .firstName("Borislav")
                .lastName("Solakov")
                .build();

        User sender = userService.registerUser(registerRequestSender);

        RegisterRequest registerRequestReceiver = RegisterRequest.builder()
                .username("solakov")
                .password("asdasd")
                .confirmPassword("asdasd")
                .email("solakov@mail.bg")
                .country(Country.BULGARIA)
                .firstName("Borislav")
                .lastName("Solakov")
                .build();

        User userReceiver = userService.registerUser(registerRequestReceiver);

        Wallet walletSender = walletRepository.findByOwnerId(sender.getId());

        Wallet walletReceiver = walletRepository.findByOwnerId(userReceiver.getId());

        assertNotNull(sender);
        assertNotNull(walletSender);
        assertEquals(walletSender.getOwner().getId(), sender.getId());
        assertNotNull(userReceiver);
        assertNotNull(walletReceiver);
        assertEquals(walletReceiver.getOwner().getId(), userReceiver.getId());
        AddFundsRequest addFundsRequest = AddFundsRequest.builder()
                .cardNumber("1231231231231231")
                .ownerName("Borislav Solakov")
                .cvv("123")
                .expireDate(LocalDate.now())
                .amount(BigDecimal.valueOf(1000))
                .build();

        walletService.addFunds(walletSender, addFundsRequest);


        TransferRequest transferRequest = TransferRequest.builder()
                .receiverUsername(userReceiver.getUsername())
                .amount(BigDecimal.valueOf(1000.0))
                .transferDescription("")
                .build();

        Transaction transferFunds = walletService.transferFunds(sender, transferRequest);
        assertNotNull(transferFunds);
        assertThat(transferFunds.getStatus()).isEqualTo(TransactionStatus.SUCCEEDED);


    }
}
