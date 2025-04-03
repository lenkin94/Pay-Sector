package app.paysector.loan.service;

import app.paysector.transaction.model.Transaction;
import jakarta.transaction.Transactional;
import app.paysector.loan.model.Loan;
import app.paysector.loan.model.LoanStatus;
import app.paysector.loan.repository.LoanRepository;
import app.paysector.transaction.model.TransactionStatus;
import app.paysector.transaction.model.TransactionType;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.LoanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserService userService;
    private final WalletService walletService;
    private final TransactionService transactionService;
    private static final double NORMAL_INTEREST = 0.1;
    private static final double LOYAL_INTEREST = 0.075;
    private static final String PAYSECTOR_LTD = "PaySector LTD";

    @Autowired
    public LoanService(LoanRepository loanRepository, UserService userService, WalletService walletService, TransactionService transactionService) {
        this.loanRepository = loanRepository;
        this.userService = userService;
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    private boolean isFailedTransaction(Wallet wallet, BigDecimal toPay) {
        return wallet.getBalance().compareTo(toPay) < 0;
    }


    public Transaction fullRepayment(UUID loanId, UUID userId) {
        User user = userService.getById(userId);
        Loan loan = getById(loanId);
        Wallet wallet = walletService.findByOwnerId(userId);

        BigDecimal toPay = loan.getAmountUntilFUllRepayment();

        String failureReason = "Insufficient funds to pay loan with id '%s'".formatted(loan.getId());
        String transactionDescription = "Full repayment of a loan with id '%s' for %.2f EUR".formatted(loan.getId(), loan.getAmountUntilFUllRepayment());

        if (isFailedTransaction(wallet, toPay)) {
            return transactionService.createNewTransaction(
                    user,
                    user.getUsername(),
                    PAYSECTOR_LTD,
                    toPay,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    failureReason);
        }

        walletService.updateWalletWithdrawal(wallet, toPay);
        loan.setAmountUntilFUllRepayment(BigDecimal.ZERO);
        loan.setLastPaymentDate(LocalDateTime.now());
        loan.setRequestPayment(false);
        loan.setLoanStatus(LoanStatus.COMPLETED);
        loan.setOverdueAmount(BigDecimal.ZERO);

        loanRepository.save(loan);

        return transactionService.createNewTransaction(
                user,
                user.getUsername(),
                PAYSECTOR_LTD,
                toPay,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);
    }


    public Transaction monthlyPayment(UUID loanId, UUID userId) {
        Loan loan = getById(loanId);
        User user = userService.getById(userId);

        String failureReason = "Insufficient funds to pay loan with id '%s'".formatted(loan.getId());
        String transactionDescription = "Monthly payment for %.2f EUR for loan with id '%s'".formatted(loan.getMonthlyPayment(), loan.getId());

        Wallet wallet = walletService.findByOwnerId(userId);

        BigDecimal toPay = loan.getMonthlyPayment();

        if (isFailedTransaction(wallet, toPay)) {
            return transactionService.createNewTransaction(
                    user,
                    user.getUsername(),
                    PAYSECTOR_LTD,
                    toPay,
                    wallet.getBalance(),
                    wallet.getCurrency(),
                    TransactionType.WITHDRAWAL,
                    TransactionStatus.FAILED,
                    transactionDescription,
                    failureReason);
        }

        walletService.updateWalletWithdrawal(wallet, toPay);
        loan.setMonthlyPayment(loan.getMonthlyPayment().subtract(loan.getOverdueAmount()));
        loan.setPaymentDate(loan.getPaymentDate().plusDays(30));
        loan.setAmountUntilFUllRepayment(loan.getAmountUntilFUllRepayment().subtract(toPay));
        loan.setLastPaymentDate(LocalDateTime.now());
        loan.setRequestPayment(false);
        loan.setOverdueAmount(BigDecimal.ZERO);

        if (loan.getAmountUntilFUllRepayment().compareTo(BigDecimal.ZERO) == 0) {
            loan.setLoanStatus(LoanStatus.COMPLETED);
            transactionDescription = "This was your last monthly payment for %.2f for loan with id '%s'".formatted(loan.getMonthlyPayment(), loan.getId());
        }

        loanRepository.save(loan);


        return transactionService.createNewTransaction(
                user,
                user.getUsername(),
                PAYSECTOR_LTD,
                toPay,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null);
    }


    public Loan createLoan(User user, LoanRequest loanRequest, Wallet wallet) {

        walletService.updateWalletDeposit(wallet, loanRequest.getAmount());

        String transactionDescription = "Loan from Pay Sector LTD for %.2f EUR.".formatted(loanRequest.getAmount());

        transactionService.createNewTransaction(
                user,
                PAYSECTOR_LTD,
                user.getUsername(),
                loanRequest.getAmount(),
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null
        );
        return loanRepository.save(initializeLoan(user, loanRequest));
    }

    public Loan initializeLoan(User user, LoanRequest loanRequest) {


        double interest = calculateInterest(user);

        BigDecimal perMonth = loanRequest.getAmount().divide(BigDecimal.valueOf(loanRequest.getPeriodInMonths()), RoundingMode.HALF_UP);
        BigDecimal perMonthWithInterest = (perMonth.multiply(BigDecimal.valueOf(interest))).add(perMonth).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalSum = perMonthWithInterest.multiply(BigDecimal.valueOf(loanRequest.getPeriodInMonths())).setScale(2, RoundingMode.HALF_UP);

        return Loan.builder()
                .owner(user)
                .amount(loanRequest.getAmount())
                .interest(interest)
                .startPeriod(LocalDateTime.now())
                .endPeriod(LocalDateTime.now().plusMonths(loanRequest.getPeriodInMonths()))
                .periodInMonths(loanRequest.getPeriodInMonths())
                .paymentDate(LocalDate.now().plusDays(30))
                .monthlyPayment(perMonthWithInterest)
                .finalSum(finalSum)
                .amountUntilFUllRepayment(finalSum)
                .loanStatus(LoanStatus.ACTIVE)
                .requestPayment(false)
                .lastPaymentDate(null)
                .overdueAmount(BigDecimal.ZERO)
                .build();
    }

    @Transactional
    public void createRefinance(UUID userId, LoanRequest loanRequest) {
        Wallet wallet = walletService.findByOwnerId(userId);
        User user = userService.getById(userId);

        Loan refincanceLoan = initializeLoan(user, loanRequest);

        Loan currentLoan = getLoansByOwnerId(userId).get(0);

        BigDecimal refinancedAmount = refincanceLoan.getAmount().subtract(currentLoan.getAmountUntilFUllRepayment());
        currentLoan.setLoanStatus(LoanStatus.REFINANCED);
        currentLoan.setAmountUntilFUllRepayment(BigDecimal.ZERO);
        currentLoan.setLastPaymentDate(LocalDateTime.now());
        currentLoan.setRequestPayment(false);



        walletService.updateWalletDeposit(wallet, refinancedAmount);

        loanRepository.save(currentLoan);




        String transactionDescription = "Refinance from Pay Sector LTD for %.2f EUR.".formatted(refincanceLoan.getAmount());

        transactionService.createNewTransaction(
                user,
                PAYSECTOR_LTD,
                user.getUsername(),
                refinancedAmount,
                wallet.getBalance(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                TransactionStatus.SUCCEEDED,
                transactionDescription,
                null
        );

        loanRepository.save(refincanceLoan);
    }

    public Loan initializeRefinance(UUID userId, LoanRequest loanRequest) {

        User user = userService.getById(userId);

        Loan loan = getLoansByOwnerId(userId).get(0);

        double interest = calculateInterest(user);

        BigDecimal amount = loanRequest.getAmount().add(loan.getAmountUntilFUllRepayment());

        BigDecimal perMonth = amount.divide(BigDecimal.valueOf(loanRequest.getPeriodInMonths()), RoundingMode.HALF_UP);
        BigDecimal perMonthWithInterest = (perMonth.multiply(BigDecimal.valueOf(interest))).add(perMonth).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalSum = perMonthWithInterest.multiply(BigDecimal.valueOf(loanRequest.getPeriodInMonths())).setScale(2, RoundingMode.HALF_UP);

        return Loan.builder()
                .owner(user)
                .amount(amount)
                .interest(interest)
                .startPeriod(LocalDateTime.now())
                .endPeriod(LocalDateTime.now().plusMonths(loanRequest.getPeriodInMonths()))
                .periodInMonths(loanRequest.getPeriodInMonths())
                .paymentDate(LocalDate.now().plusDays(30))
                .monthlyPayment(perMonthWithInterest)
                .finalSum(finalSum)
                .amountUntilFUllRepayment(finalSum)
                .loanStatus(LoanStatus.ACTIVE)
                .requestPayment(false)
                .lastPaymentDate(null)
                .overdueAmount(BigDecimal.ZERO)
                .build();
    }

    public double calculateInterest(User user) {
        if (LocalDate.now().getYear() - user.getCreatedAt().getYear() >= 3 &&
                LocalDate.now().getMonthValue() >= user.getCreatedAt().getMonthValue() &&
                LocalDate.now().getDayOfMonth() >= user.getCreatedAt().getDayOfMonth()) {
            return LOYAL_INTEREST;
        }
        return NORMAL_INTEREST;
    }


    public List<Loan> getLoansByOwnerId(UUID userId) {
        return loanRepository.findAllByOwnerIdOrderByStartPeriodDesc(userId);
    }

    public Loan getById(UUID loanId) {
        return loanRepository.findById(loanId).orElseThrow(() -> new RuntimeException("Loan not found"));
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    public void updateLoan(Loan loan) {
        loanRepository.save(loan);
    }

    public Loan getLoanToRefinance(List<Loan> loans) {
        return loans.get(0);
    }
}
