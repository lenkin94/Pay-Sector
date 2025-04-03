package app.paysector.web;

import jakarta.validation.Valid;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.AddFundsRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/wallet")
public class WalletController {
    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public WalletController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping
    public ModelAndView getWalletPage(@AuthenticationPrincipal AuthenticateUser authenticateUser) {

        User user = userService.getById(authenticateUser.getUserId());


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("wallet");
        modelAndView.addObject("user", user);


        return modelAndView;
    }

    @GetMapping("/add-funds")
    public ModelAndView addFunds(@AuthenticationPrincipal AuthenticateUser authenticateUser, AddFundsRequest addFundsRequest) {

        User user = userService.getById(authenticateUser.getUserId());
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("add-funds");
        modelAndView.addObject("addFundsRequest", addFundsRequest);
        modelAndView.addObject("user", user);

        return modelAndView;
    }

    @PutMapping("/add-funds")
    public String addFunds(@AuthenticationPrincipal AuthenticateUser authenticateUser, @Valid AddFundsRequest addFundsRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "add-funds";
        }

        Wallet wallet = walletService.findByOwnerId(authenticateUser.getUserId());

        walletService.addFunds(wallet, addFundsRequest);

        return "redirect:/wallet";
    }
}
