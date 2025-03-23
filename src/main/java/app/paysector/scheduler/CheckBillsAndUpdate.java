package app.paysector.scheduler;

import app.paysector.bill.model.Bill;
import app.paysector.bill.service.BillService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CheckBillsAndUpdate {

    private final BillService billService;

    public CheckBillsAndUpdate(BillService billService) {
        this.billService = billService;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void checkBillsAndUpdate() {

        List<Bill> allBills = billService.allBills();

        for (Bill bill : allBills) {

            if (LocalDate.now().getMonth().getValue() > bill.getEndPeriod().plusMonths(1).getMonth().getValue()) {
                if (bill.isPaid()) {
                    bill.setAmount(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(30, 400)));
                    bill.setStartPeriod(bill.getStartPeriod().plusMonths(1));
                    bill.setEndPeriod(LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).getMonth().length(LocalDate.now().isLeapYear())));
                    bill.setPayedOn(null);
                    bill.setPaid(false);

                } else {
                    bill.setAmount(bill.getAmount().add(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(30, 400))));
                    bill.setEndPeriod(LocalDate.now().minusMonths(1).withDayOfMonth(LocalDate.now().minusMonths(1).getMonth().length(LocalDate.now().isLeapYear())));
                }
            }

            billService.updateBill(bill);
        }
    }
}
