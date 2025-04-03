package app.paysector.web;

import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.model.UserRole;
import app.paysector.user.service.UserService;
import app.paysector.web.dto.EditProfileRequest;
import app.paysector.web.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static app.paysector.web.TestBuilder.randomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerApiTest {
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Test
    void getRequestEditProfileEndpoint_returnEditProfilePage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/users/{userId}/edit-profile", userId)
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("edit-profile"))
                .andExpect(model().attributeExists("editProfileDetails"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getById(any());
    }

    @Test
    void putRequestEditProfileEndpoint_returnSuccessfullyEditedProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{userId}/edit-profile", userId)
                .formField("firstName", "Borislav")
                .formField("lastName", "Solakov")
                .formField("email", "lenkin@mail.bg")
                .formField("profilePicture", "")
                .formField("country", "BULGARIA")
                .with(user(principal))
                .with(csrf());

        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));

        verify(userService,times(1)).editProfile(any(), any());
    }

    @Test
    void putRequestWithErrorParamEditProfileEndpoint_returnEditProfilePage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{userId}/edit-profile", userId)
                .formField("firstName", "a")
                .formField("lastName", "Solakov")
                .formField("email", "lenkin@mail.bg")
                .formField("profilePicture", "")
                .formField("country", "BULGARIA")
                .with(user(principal))
                .with(csrf());

        mvc.perform(request)
                .andExpect(model().attributeExists("editProfileDetails"))
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("edit-profile"));

        verify(userService, never()).editProfile(any(), any());
        verify(userService, times(1)).getById(any());
    }

    @Test
    void getRequestChangePasswordEndpoint_returnChangePasswordPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/users/{userId}/edit-profile/change-password", userId)
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("change-password"))
                .andExpect(model().attributeExists("changePasswordRequest"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getById(any());
    }

    @Test
    void putRequestChangePasswordEndpoint_returnSuccessfullyChangedPassword() throws Exception {
        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/users/{userId}/edit-profile/change-password", userId)
                .formField("oldPassword", "asdasd")
                .formField("newPassword", "dsadsa")
                .formField("confirmPassword", "dsadsa")
                .with(user(principal))
                .with(csrf());

        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/home"));

        verify(userService,times(1)).changePassword(any(), any());
    }

    @Test
    void getAllUsersEndpointUnauthorized_returnBadRequest() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        List<User> users = List.of(randomUser(), randomUser());
        when(userService.getAllUsers()).thenReturn(users);
        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().is5xxServerError())
                .andExpect(view().name("bad-request"));
        verify(userService, never()).getById(any());
        verify(userService, never()).getAllUsers();
    }

    @Test
    void getAllUsersEndpointAuthorized_returnAllUsersPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        List<User> users = List.of(randomUser(), randomUser());
        when(userService.getAllUsers()).thenReturn(users);
        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/users")
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(any());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getRequestUserDetailsUnauthorized_returnBadRequest() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/users/{userId}/details", userId)
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().is5xxServerError())
                .andExpect(view().name("bad-request"));
        verify(userService, never()).getById(any());
    }

    @Test
    void getRequestUserDetailsAuthorized_returnUserDetails() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = get("/users/{userId}/details", userId)
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("user-details"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(2)).getById(any());
    }

    @Test
    void putRequestChangeStatusAuthorized_returnSuccessfullySwitchedStatus() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/users/{userId}/change-status", userId)
                .with(user(principal))
                .with(csrf());

        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/" + userId + "/details"));
        verify(userService, times(1)).changeStatus(any());
    }

    @Test
    void putRequestChangeRoleAuthorized_returnSuccessfullySwitchedStatus() throws Exception {
        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.ADMIN, true);
        MockHttpServletRequestBuilder request = put("/users/{userId}/change-role", userId)
                .with(user(principal))
                .with(csrf());

        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/" + userId + "/details"));
        verify(userService, times(1)).changeRole(any());
    }
}
