package app.paysector.scheduler;

import app.paysector.loan.model.Loan;
import app.paysector.loan.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class CheckLoanAndUpdate {

    private final LoanService loanService;
    private static final double OVERDUE_INTEREST = 0.10;

    @Autowired
    public CheckLoanAndUpdate(LoanService loanService) {
        this.loanService = loanService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void checkForOverdueAndPaymentRequest() {
        List<Loan> allLoans = loanService.getAllLoans();
        for (Loan loan : allLoans) {
            if (LocalDate.now().isAfter(loan.getPaymentDate()) && loan.isRequestPayment()) {
                BigDecimal overdue = loan.getMonthlyPayment().multiply(BigDecimal.valueOf(OVERDUE_INTEREST));

                loan.setOverdueAmount(loan.getOverdueAmount().add(overdue));
                loan.setMonthlyPayment(loan.getMonthlyPayment().add(overdue));
                loan.setAmountUntilFUllRepayment(loan.getAmountUntilFUllRepayment().add(overdue));
            }

            if (loan.getPaymentDate().minusDays(7).getDayOfMonth() == LocalDateTime.now().getDayOfMonth() &&
                    !loan.isRequestPayment() &&
                    loan.getLoanStatus().name().equals("ACTIVE")) {

                loan.setRequestPayment(true);
            }

            loanService.updateLoan(loan);
        }
    }
}
