package app.paysector.wallet.service;

import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.repository.WalletRepository;
import app.paysector.web.dto.AddFundsRequest;
import app.paysector.web.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService,@Lazy UserService userService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
        this.userService = userService;
    }


    public Wallet createWallet(UUID userId) {

        User user = userService.getById(userId);

        Wallet wallet = Wallet.builder()
                .owner(user)
                .balance(new BigDecimal(0))
                .currency(Currency.getInstance("EUR"))
                .createdOn(LocalDateTime.now())
                .updatedOn(LocalDateTime.now())
                .build();

       walletRepository.save(wallet);

       return wallet;
    }

    @Transactional
    public void addFunds(UUID walletId, AddFundsRequest addFundsRequest) {

        Wallet wallet = getWalletById(walletId);
        String transactionDescription = "%.2f EUR added to wallet".formatted(addFundsRequest.getAmount().doubleValue());


        wallet.setBalance(wallet.getBalance().add(addFundsRequest.getAmount()));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

         transactionService.createNewTransaction(wallet.getOwner(),
                addFundsRequest.getOwnerName(),
                walletId.toString(),
                addFundsRequest.getAmount(),
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);
    }

    private Wallet getWalletById(UUID walletId) {
        return walletRepository.findById(walletId).orElseThrow(() -> new RuntimeException("Wallet not found"));
    }

    public Wallet findByOwnerId(UUID userId) {
       return walletRepository.findByOwnerId(userId);
    }

    @Transactional
    public void transferFunds(UUID senderId, TransferRequest transferRequest) {

        User sender = userService.getById(senderId);

        boolean isFailed = false;
        String failureReason = "";
        String transferDescription;

        if (transferRequest.getTransferDescription().isEmpty()) {
            transferDescription = "Transfer from '%s' to '%s', for %.2f EUR".formatted(sender.getUsername(), transferRequest.getReceiverUsername(), transferRequest.getAmount());
        } else {
            transferDescription = transferRequest.getTransferDescription();
        }

        Wallet senderWallet = walletRepository.findByOwnerId(sender.getId());



        Optional<User> optionalReceiver = userService.getByUsername(transferRequest.getReceiverUsername());

        if (optionalReceiver.isEmpty()) {
            isFailed = true;
            failureReason = "User with username '%s' not found".formatted(transferRequest.getReceiverUsername());
        }

        if (senderWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            failureReason = "Insufficient funds to transfer %.2f EUR to %s".formatted(transferRequest.getAmount(), transferRequest.getReceiverUsername());
            isFailed = true;
        }


        if (isFailed) {
             transactionService.createNewTransaction(sender,
                    sender.getUsername(),
                    transferRequest.getReceiverUsername(),
                    transferRequest.getAmount(),
                    senderWallet.getBalance(),
                    senderWallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transferDescription,
                    failureReason);

            return;
        }




        User receiver = optionalReceiver.get();
        Wallet receiverWallet = walletRepository.findByOwnerId(receiver.getId());

        updateWalletDeposit(receiverWallet.getId(), transferRequest.getAmount());
        updateWalletWithdrawal(senderWallet.getId(), transferRequest.getAmount());

        Transaction senderTransaction = transactionService.createNewTransaction(
                sender,
                sender.getUsername(),
                receiver.getUsername(),
                transferRequest.getAmount(),
                senderWallet.getBalance(),
                senderWallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                transferDescription,
                null
        );

        Transaction receiverTransaction = transactionService.createNewTransaction(
                receiver,
                sender.getUsername(),
                receiver.getUsername(),
                transferRequest.getAmount(),
                receiverWallet.getBalance(),
                receiverWallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transferDescription,
                null
        );

    }

    public void updateWalletDeposit(UUID walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);
    }

    public void updateWalletWithdrawal(UUID walletId, BigDecimal amount) {
        Wallet wallet = getWalletById(walletId);
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);
    }
}
