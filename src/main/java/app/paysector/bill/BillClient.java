package app.paysector.bill;

import app.paysector.bill.dto.AddBillRequest;
import app.paysector.bill.dto.Bill;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "bill-svc", url = "http://localhost:8081/api/v1/bills")
public interface BillClient {
    @PostMapping("add-bill")
    ResponseEntity<Void> addBill(@RequestBody AddBillRequest addBillRequest);

    @PutMapping("{billId}/pay")
    ResponseEntity<Void> payBill(@PathVariable("billId") UUID billId);

    @DeleteMapping("{userId}/{billId}/remove")
    ResponseEntity<Void> removeBill(@PathVariable("userId") UUID userId, @PathVariable("billId") UUID billId);

    @GetMapping("{userId}/all-bills")
    ResponseEntity<List<Bill>> getAllUserBills(@PathVariable("userId") UUID userId);

    @GetMapping("/{billId}")
    ResponseEntity<Bill> getBill(@PathVariable("billId") UUID billId);
}
