package app.paysector.web;

import app.paysector.security.AuthenticateUser;
import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView getTransactions(@AuthenticationPrincipal AuthenticateUser authenticateUser) {

        User user = userService.getById(authenticateUser.getUserId());
        List<Transaction> transactions = transactionService.findByOwnerId(authenticateUser.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transactions");
        modelAndView.addObject("transactions", transactions);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

}
