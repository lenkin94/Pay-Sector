package app.paysector.web;

import app.paysector.transaction.model.Transaction;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import jakarta.validation.Valid;
import app.paysector.loan.model.Loan;
import app.paysector.loan.service.LoanService;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.web.dto.LoanRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/loan")
public class LoanController {

    private final UserService userService;
    private final LoanService loanService;
    private final WalletService walletService;

    @Autowired
    public LoanController(UserService userService, LoanService loanService, WalletService walletService) {
        this.userService = userService;
        this.loanService = loanService;
        this.walletService = walletService;
    }

    @GetMapping
    public ModelAndView getLoansPage(@AuthenticationPrincipal AuthenticateUser authenticateUser) {

        User user = userService.getById(authenticateUser.getUserId());
        List<Loan> loans = loanService.getLoansByOwnerId(authenticateUser.getUserId());


        ModelAndView mav = new ModelAndView("loan");
        mav.addObject("user", user);
        mav.addObject("loans", loans);

        return mav;
    }

    @GetMapping("request-loan")
    public ModelAndView getRequestLoanPage(@AuthenticationPrincipal AuthenticateUser authenticateUser, LoanRequest loanRequest) {
        User user = userService.getById(authenticateUser.getUserId());


        ModelAndView mav = new ModelAndView("request-loan");
        mav.addObject("loanRequest", loanRequest);
        mav.addObject("user", user);

        return mav;
    }

    @GetMapping("offer-details")
    public ModelAndView getOfferDetailsPage(@AuthenticationPrincipal AuthenticateUser authenticateUser, LoanRequest loanRequest) {
        User user = userService.getById(authenticateUser.getUserId());
        Loan loanDetails = loanService.initializeLoan(user, loanRequest);

        ModelAndView mav = new ModelAndView("offer-details");
        mav.addObject("user", user);
        mav.addObject("loanDetails", loanDetails);

        return mav;
    }

    @PostMapping("accept-details")
    public String acceptLoanDetails(@AuthenticationPrincipal AuthenticateUser authenticateUser, @Valid LoanRequest loanRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "redirect:/loan/request-loan";
        }
        User user = userService.getById(authenticateUser.getUserId());
        Wallet wallet = walletService.findByOwnerId(user.getId());
        Loan loan = loanService.createLoan(user, loanRequest, wallet);


        return "redirect:/loan";
    }

    @GetMapping("{loanId}/info")
    public ModelAndView loanInfo(@PathVariable UUID loanId, @AuthenticationPrincipal AuthenticateUser authenticateUser) {
        User user = userService.getById(authenticateUser.getUserId());
        Loan loan = loanService.getById(loanId);

        ModelAndView mav = new ModelAndView("loan-info");
        mav.addObject("loan", loan);
        mav.addObject("user", user);

        return mav;
    }

    @PutMapping("{loanId}/pay")
    public String monthlyPayment(@PathVariable UUID loanId, @AuthenticationPrincipal AuthenticateUser authenticateUser) {

        Transaction monthlyPayment = loanService.monthlyPayment(loanId, authenticateUser.getUserId());

        return "redirect:/transactions";
    }

    @PutMapping("{loanId}/full-repayment")
    public String fullRepayment(@PathVariable UUID loanId, @AuthenticationPrincipal AuthenticateUser authenticateUser) {

        Transaction fullRepayment = loanService.fullRepayment(loanId, authenticateUser.getUserId());

        return "redirect:/transactions";
    }

    @GetMapping("refinance-loan")
    public ModelAndView getRefinanceLoanPage(@AuthenticationPrincipal AuthenticateUser authenticateUser, LoanRequest loanRequest) {
        User user = userService.getById(authenticateUser.getUserId());
        Loan loanToRefinance = loanService.getLoanToRefinance(loanService.getLoansByOwnerId(user.getId()));
        ModelAndView mav = new ModelAndView("refinance-loan");
        mav.addObject("loanRequest", loanRequest);
        mav.addObject("user", user);
        mav.addObject("loanToRefinance", loanToRefinance);

        return mav;
    }

    @GetMapping("refinance-details")
    public ModelAndView getRefinanceDetailsPage(@AuthenticationPrincipal AuthenticateUser authenticateUser, LoanRequest loanRequest) {
        User user = userService.getById(authenticateUser.getUserId());
        Loan loanDetails = loanService.initializeRefinance(authenticateUser.getUserId(), loanRequest);
        Loan loanToRefinance = loanService.getLoanToRefinance(loanService.getLoansByOwnerId(authenticateUser.getUserId()));

        ModelAndView mav = new ModelAndView("refinance-details");
        mav.addObject("loanDetails", loanDetails);
        mav.addObject("loanToRefinance", loanToRefinance);
        mav.addObject("user", user);

        return mav;
    }

    @PostMapping("accept-refinance")
    public String acceptRefinanceDetails(@AuthenticationPrincipal AuthenticateUser authenticateUser, @Valid LoanRequest loanRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "redirect:/loan/refinance-loan";
        }

        loanService.createRefinance(authenticateUser.getUserId(), loanRequest);


        return "redirect:/loan";
    }
}
