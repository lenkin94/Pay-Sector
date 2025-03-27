package app.paysector.web;

import app.paysector.bill.dto.Bill;
import jakarta.validation.Valid;
//import app.paysector.bill.dto.Bill;
import app.paysector.bill.service.BillService;
import app.paysector.loan.model.Loan;
import app.paysector.loan.service.LoanService;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.web.dto.LoginRequest;
import app.paysector.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class IndexController {

    private final UserService userService;
    private final LoanService loanService;
    private final BillService billService;

    @Autowired
    public IndexController(UserService userService, LoanService loanService, BillService billService) {
        this.userService = userService;
        this.loanService = loanService;
        this.billService = billService;
    }

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }


    @GetMapping("/register")
    public ModelAndView getRegisterPage(RegisterRequest registerRequest) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("register");
        modelAndView.addObject("registerRequest", registerRequest);

        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView registerNewUser(@Valid RegisterRequest registerRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("register");
        }

        userService.registerUser(registerRequest);


      return new ModelAndView("redirect:/login");

    }

    @GetMapping("/login")
    public ModelAndView getLoginPage(@RequestParam(value = "error", required = false) String errorParam) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("login");
        modelAndView.addObject("loginRequest", new LoginRequest());

        if (errorParam != null) {
            modelAndView.addObject("errorMessage", "Incorrect username or password!");
        }

        return modelAndView;
    }

    @GetMapping("/home")
    public ModelAndView getHomePage(@AuthenticationPrincipal AuthenticateUser authenticateUser) {

        User user = userService.getById(authenticateUser.getUserId());
        List<Loan> loans = loanService.getLoansByOwnerId(user.getId());
        List<Bill> bills = billService.allUserBills(authenticateUser.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("loans", loans);
        modelAndView.addObject("bills", bills);

        return modelAndView;
    }

}
