package app.paysector.wallet.service;

import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.repository.WalletRepository;
import app.paysector.web.dto.AddFundsRequest;
import app.paysector.web.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public WalletService(WalletRepository walletRepository, TransactionService transactionService) {
        this.walletRepository = walletRepository;
        this.transactionService = transactionService;
    }


    public Wallet createWallet(User user) {


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
    public void addFunds(Wallet wallet, AddFundsRequest addFundsRequest) {


        String transactionDescription = "%.2f EUR added to wallet".formatted(addFundsRequest.getAmount().doubleValue());


        wallet.setBalance(wallet.getBalance().add(addFundsRequest.getAmount()));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);

         transactionService.createNewTransaction(wallet.getOwner(),
                addFundsRequest.getOwnerName(),
                wallet.getId().toString(),
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
    public Transaction transferFunds(User sender, TransferRequest transferRequest) {

        boolean isFailed = false;
        String failureReason = "";
        String transferDescription = "";

        if (transferRequest.getTransferDescription().isEmpty()) {
            transferDescription = "Transfer from '%s' to '%s', for %.2f EUR".formatted(sender.getUsername(), transferRequest.getReceiverUsername(), transferRequest.getAmount());
        }

        Wallet senderWallet = walletRepository.findByOwnerId(sender.getId());

        Optional<Wallet> optionalReceiver = walletRepository.findByOwnerUsername(transferRequest.getReceiverUsername());



        if (optionalReceiver.isEmpty()) {
            isFailed = true;
            failureReason = "User with username '%s' not found".formatted(transferRequest.getReceiverUsername());
        }

        if (senderWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            failureReason = "Insufficient funds to transfer %.2f EUR to %s".formatted(transferRequest.getAmount(), transferRequest.getReceiverUsername());
            isFailed = true;
        }


        if (isFailed) {
             return transactionService.createNewTransaction(sender,
                    sender.getUsername(),
                    transferRequest.getReceiverUsername(),
                    transferRequest.getAmount(),
                    senderWallet.getBalance(),
                    senderWallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transferDescription,
                    failureReason);
        }




        Wallet receiverWallet = optionalReceiver.get();


        updateWalletDeposit(receiverWallet, transferRequest.getAmount());
        updateWalletWithdrawal(senderWallet, transferRequest.getAmount());



        Transaction receiverTransaction = transactionService.createNewTransaction(
                receiverWallet.getOwner(),
                sender.getUsername(),
                transferRequest.getReceiverUsername(),
                transferRequest.getAmount(),
                receiverWallet.getBalance(),
                receiverWallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transferDescription,
                null
        );
        return transactionService.createNewTransaction(
                sender,
                sender.getUsername(),
                transferRequest.getReceiverUsername(),
                transferRequest.getAmount(),
                senderWallet.getBalance(),
                senderWallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                transferDescription,
                null
        );
    }

    public void updateWalletDeposit(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);
    }

    public void updateWalletWithdrawal(Wallet wallet, BigDecimal amount) {
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setUpdatedOn(LocalDateTime.now());

        walletRepository.save(wallet);
    }
}
