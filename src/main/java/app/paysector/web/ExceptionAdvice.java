package app.paysector.web;

import app.paysector.exception.BillsFeignException;
import app.paysector.exception.EmailAlreadyExistsException;
import app.paysector.exception.PasswordsDoNotMatchException;
import app.paysector.exception.UsernameAlreadyExistsException;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import ch.qos.logback.core.model.Model;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class ExceptionAdvice {

    private final UserService userService;

    public ExceptionAdvice(UserService userService) {
        this.userService = userService;
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public String handleUsernameAlreadyExist(RedirectAttributes redirectAttributes, UsernameAlreadyExistsException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("usernameAlreadyExistsMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public String handleEmailAlreadyExist(RedirectAttributes redirectAttributes, EmailAlreadyExistsException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("emailAlreadyExistsMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(PasswordsDoNotMatchException.class)
    public String handlePasswordsDoNotMatch(RedirectAttributes redirectAttributes, PasswordsDoNotMatchException exception) {

        String message = exception.getMessage();

        redirectAttributes.addFlashAttribute("passwordsDoNotMatchMessage", message);
        return "redirect:/register";
    }

    @ExceptionHandler(HttpClientErrorException.Forbidden.class)
    public ModelAndView handleForbidden(HttpClientErrorException exception) {
        return new ModelAndView("bad-request");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAnyException(Exception exception) {


        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("bad-request");
        modelAndView.addObject("errorMessage", exception.getClass().getSimpleName());


        return modelAndView;
    }

    @ExceptionHandler(BillsFeignException.class)
    public String handleBillsFeignException() {

        return "redirect:/bad-request";
    }
}
