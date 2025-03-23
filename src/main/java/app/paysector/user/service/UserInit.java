package app.paysector.user.service;

import app.paysector.user.model.Country;
import app.paysector.user.model.User;
import app.paysector.user.model.UserRole;
import app.paysector.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class UserInit implements CommandLineRunner {

    private final UserService userService;

    @Autowired
    public UserInit(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {

        if (userService.getAllUsers().isEmpty()) {
            RegisterRequest build = RegisterRequest.builder()
                    .firstName("Borislav")
                    .lastName("Solakov")
                    .username("admin")
                    .password("admin")
                    .confirmPassword("admin")
                    .email("admin@paysector.com")
                    .country(Country.BULGARIA)
                    .build();

            User user = userService.registerUser(build);

            user.setRole(UserRole.ADMIN);
            userService.updateUser(user);
        }
    }
}
