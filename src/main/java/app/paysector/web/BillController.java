package app.paysector.web;


import app.paysector.bill.dto.Bill;
import app.paysector.transaction.model.Transaction;
import jakarta.validation.Valid;
import app.paysector.bill.service.BillService;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.bill.dto.AddBillRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/bills")
public class BillController {

    private final UserService userService;
    private final BillService billService;

    @Autowired
    public BillController(UserService userService, BillService billService) {
        this.userService = userService;
        this.billService = billService;
    }

    @GetMapping
    private ModelAndView getBillsPage(@AuthenticationPrincipal AuthenticateUser authenticateUser) {

        User user = userService.getById(authenticateUser.getUserId());
        List<Bill> bills = billService.allUserBills(authenticateUser.getUserId());


        ModelAndView mav = new ModelAndView();
        mav.setViewName("bills");
        mav.addObject("bills", bills);
        mav.addObject("user", user);


        return mav;
    }

    @PutMapping("/{id}/pay")
    private String payBill(@PathVariable UUID id, @AuthenticationPrincipal AuthenticateUser authenticateUser) {
        Transaction payBill = billService.payBill(id, authenticateUser.getUserId());

        return "redirect:/bills";
    }

    @GetMapping("add-bill")
    private ModelAndView addBillPage(@AuthenticationPrincipal AuthenticateUser authenticateUser, AddBillRequest addBillRequest) {
        User user = userService.getById(authenticateUser.getUserId());
        ModelAndView mav = new ModelAndView();
        mav.setViewName("add-bill");
        mav.addObject("bill", addBillRequest);
        mav.addObject("user", user);

        return mav;
    }

    @PostMapping("add-bill")
    private String addBill(@AuthenticationPrincipal AuthenticateUser authenticateUser, @Valid AddBillRequest addBillRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "add-bill";
        }

        User user = userService.getById(authenticateUser.getUserId());

        billService.addBill(user, addBillRequest);

        return "redirect:/bills";
    }

    @DeleteMapping("{userId}/{billId}/remove")
    private String removeBill(@PathVariable UUID userId, @PathVariable UUID billId) {

        billService.removeBill(billId, userId);

        return "redirect:/bills";
    }
}
