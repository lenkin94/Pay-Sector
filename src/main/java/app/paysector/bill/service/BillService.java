package app.paysector.bill.service;

import app.paysector.bill.model.Bill;
import app.paysector.bill.repository.BillRepository;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.AddBillRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class BillService {

    private final BillRepository billRepository;
    private final TransactionService transactionService;
    private final WalletService walletService;
    private static final String PAYSECTOR_LTD = "PaySector LTD";

    @Autowired
    public BillService(BillRepository billRepository, TransactionService transactionService, WalletService walletService) {
        this.billRepository = billRepository;
        this.transactionService = transactionService;
        this.walletService = walletService;
    }

    public void addBill(User user, AddBillRequest request) {
        Optional<Bill> optionalBill = billRepository.findByBillNumber(request.getBillNumber());

        if (optionalBill.isPresent()) {
            throw new RuntimeException("Bill already exists");
        }

        billRepository.save(createBill(request, user));
    }


    public Bill createBill(AddBillRequest addBillRequest, User user) {

        return Bill.builder()
                .owner(user)
                .billNumber(addBillRequest.getBillNumber())
                .billType(addBillRequest.getBillType())
                .startPeriod(LocalDate.now().minusMonths(1).withDayOfMonth(1))
                .endPeriod(LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).getMonth().length(LocalDate.now().isLeapYear())))
                .isPaid(false)
                .amount(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(30, 400)))
                .build();
    }


    public List<Bill> findByOwnerId(UUID id) {
       return billRepository.findByOwnerId(id);
    }

    public Bill findById(UUID id) {
        return billRepository.findById(id).orElseThrow(() -> new RuntimeException("Bill not found"));
    }

    @Transactional
    public void payBill(UUID billId, User user) {
        Bill bill = findById(billId);
        Wallet wallet = walletService.findByOwnerId(user.getId());

        String failureReason = "Insufficient funds to pay %s bill with account number %s".formatted(bill.getBillType(), bill.getBillNumber());
        String transactionDescription = "Paying %s bill with account number %s.".formatted(bill.getBillType(), bill.getBillNumber());

        if (wallet.getBalance().compareTo(bill.getAmount()) < 0) {
            transactionService.createNewTransaction(user,
                    user.getUsername(),
                    PAYSECTOR_LTD,
                    bill.getAmount(),
                    user.getWallet().getBalance(),
                    user.getWallet().getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    failureReason);
            return;
        }



        walletService.updateWalletWithdrawal(wallet.getId(), bill.getAmount());
        bill.setPaid(true);
        bill.setPayedOn(LocalDate.now());

        transactionService.createNewTransaction(user,
                user.getUsername(),
                PAYSECTOR_LTD,
                bill.getAmount(),
                user.getWallet().getBalance(),
                user.getWallet().getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);


        billRepository.save(bill);
    }

    public void updateBill(Bill bill) {
        billRepository.save(bill);
    }

    public List<Bill> allBills() {
        return billRepository.findAll();
    }
}
