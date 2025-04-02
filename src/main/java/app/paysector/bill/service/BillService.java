package app.paysector.bill.service;

import app.paysector.bill.BillClient;
//import app.paysector.bill.dto.Bill;
//import app.paysector.bill.repository.BillRepository;
import app.paysector.bill.dto.Bill;
import app.paysector.bill.dto.GetAllUserBills;
import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.bill.dto.AddBillRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRange;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class BillService {

//    private final BillRepository billRepository;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private static final String PAYSECTOR_LTD = "PaySector LTD";
    private final BillClient billClient;
    private final UserService userService;

    @Autowired
    public BillService(TransactionService transactionService, WalletService walletService, BillClient billClient, UserService userService) {
        this.transactionService = transactionService;
        this.walletService = walletService;
        this.billClient = billClient;
        this.userService = userService;
    }

    public void addBill(User user, AddBillRequest request) {

        AddBillRequest addBillRequest = AddBillRequest.builder()
                .userId(user.getId())
                .billNumber(request.getBillNumber())
                .billType(request.getBillType())
                .build();

        try {
            billClient.addBill(addBillRequest);
        } catch (Exception e) {
            log.error("Unable to add bill");
        }
    }

    public List<Bill> allUserBills(UUID userId) {
        ResponseEntity<List<Bill>> response = billClient.getAllUserBills(userId);


        return response.getBody();
    }


    @Transactional
    public Transaction payBill(UUID billId, UUID userId) {
        User user = userService.getById(userId);

        ResponseEntity<Bill> response = billClient.getBill(billId);

        Bill bill = response.getBody();

        Wallet wallet = walletService.findByOwnerId(user.getId());

        String failureReason = "Insufficient funds to pay %s bill with account number %s".formatted(bill.getBillType(), bill.getBillNumber());
        String transactionDescription = "Paying %s bill with account number %s.".formatted(bill.getBillType(), bill.getBillNumber());

        if (wallet.getBalance().compareTo(bill.getAmount()) < 0) {
            return transactionService.createNewTransaction(user,
                    user.getUsername(),
                    PAYSECTOR_LTD,
                    bill.getAmount(),
                    user.getWallet().getBalance(),
                    user.getWallet().getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    failureReason);
        }


        walletService.updateWalletWithdrawal(wallet, bill.getAmount());

        try {
            billClient.payBill(billId);
        } catch (Exception e) {
            log.error("Paying bill failed");
        }

        return transactionService.createNewTransaction(user,
                user.getUsername(),
                PAYSECTOR_LTD,
                bill.getAmount(),
                user.getWallet().getBalance(),
                user.getWallet().getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);

    }

    public void removeBill(UUID billId, UUID userId) {
        try {
            billClient.removeBill(userId, billId);
        } catch (Exception e) {
            log.error("Unable to remove bill");
        }
    }
}
