package app.paysector.transaction.service;

import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.repository.TransactionRepository;
import app.paysector.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction createNewTransaction(User owner, String sender, String receiver, BigDecimal transactionAmount, BigDecimal balanceLeft, Currency currency, TransactionType type, TransactionStatus status, String transactionDescription, String failureReason) {

        Transaction transaction = Transaction.builder()
                .owner(owner)
                .sender(sender)
                .receiver(receiver)
                .amount(transactionAmount)
                .balanceLeft(balanceLeft)
                .currency(currency)
                .type(type)
                .status(status)
                .description(transactionDescription)
                .failureReason(failureReason)
                .createdOn(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }


    public List<Transaction> findByOwnerId(UUID userId) {
        return transactionRepository.findAllByOwnerIdOrderByCreatedOnDesc(userId);
    }


}
