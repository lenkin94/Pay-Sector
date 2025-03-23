package app.paysector.web;

import jakarta.validation.Valid;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.service.UserService;
import app.paysector.web.dto.ChangePasswordRequest;
import app.paysector.web.dto.EditProfileRequest;
import app.paysector.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/edit-profile")
    public ModelAndView getProfileEditPage(@PathVariable UUID userId) {

        User user = userService.getById(userId);

        ModelAndView mav = new ModelAndView("edit-profile");
        mav.addObject("user", user);
        mav.addObject("editProfileDetails", DtoMapper.mapUserToUserEditRequest(user));


        return mav;
    }

    @PutMapping("/{userId}/edit-profile")
    public ModelAndView editProfile(@PathVariable UUID userId, @Valid EditProfileRequest editProfileRequest, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            User user = userService.getById(userId);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.setViewName("edit-profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("userEditRequest", editProfileRequest);
            return modelAndView;
        }

        userService.editProfile(userId, editProfileRequest);

        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/{userId}/edit-profile/change-password")
    public ModelAndView getChangePasswordPage(@PathVariable UUID userId, @Valid ChangePasswordRequest changePasswordRequest) {

        User user = userService.getById(userId);

        ModelAndView mav = new ModelAndView("change-password");
        mav.addObject("user", user);
        mav.addObject("changePasswordRequest", changePasswordRequest);


        return mav;
    }

    @PutMapping("/{userId}/edit-profile/change-password")
    public String changePassword(@PathVariable UUID userId, @Valid ChangePasswordRequest changePasswordRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "change-password";
        }

        userService.changePassword(userId, changePasswordRequest);

        return "redirect:/home";
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ModelAndView getAllUsersPage(@AuthenticationPrincipal AuthenticateUser authenticateUser) {
        User user = userService.getById(authenticateUser.getUserId());
        List<User> users = userService.getAllUsers().stream().filter(u -> !u.getId().equals(user.getId())).toList();


        ModelAndView mav = new ModelAndView("users");
        mav.addObject("users", users);
        mav.addObject("user", user);

        return mav;
    }

    @GetMapping("/{userId}/details")
    public ModelAndView getUserDetails(@AuthenticationPrincipal AuthenticateUser authenticateUser,@PathVariable UUID userId) {

        User userDetails = userService.getById(userId);
        User user = userService.getById(authenticateUser.getUserId());

        ModelAndView mav = new ModelAndView("user-details");
        mav.addObject("userDetails", userDetails);
        mav.addObject("user", user);

        return mav;
    }

    @PutMapping("/{userId}/change-status")
    public String changeStatus(@PathVariable UUID userId) {

        userService.changeStatus(userId);

        return "redirect:/users/" + userId + "/details";
    }

    @PutMapping("/{userId}/change-role")
    public String changeRole(@PathVariable UUID userId) {

        userService.changeRole(userId);

        return "redirect:/users/" + userId + "/details";
    }
}
