package app.paysector.web;

import jakarta.validation.Valid;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/transfers")
public class TransferController {

    private final UserService userService;
    private final WalletService walletService;

    @Autowired
    public TransferController(UserService userService, WalletService walletService) {
        this.userService = userService;
        this.walletService = walletService;
    }

    @GetMapping
    public ModelAndView getTransferPage(@AuthenticationPrincipal AuthenticateUser authenticateUser) {

        User user = userService.getById(authenticateUser.getUserId());

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("transfer");
        modelAndView.addObject("user", user);
        modelAndView.addObject("transferRequest", new TransferRequest());

        return modelAndView;
    }

    @PostMapping
    public ModelAndView transfer(@Valid TransferRequest transferRequest, @AuthenticationPrincipal AuthenticateUser authenticateUser, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ModelAndView("transfer");
        }

        walletService.transferFunds(authenticateUser.getUserId(), transferRequest);

        return new ModelAndView("redirect:/transactions");
    }
}
