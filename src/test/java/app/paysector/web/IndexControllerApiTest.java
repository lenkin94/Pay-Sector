package app.paysector.web;

import app.paysector.bill.service.BillService;
import app.paysector.loan.service.LoanService;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.UserRole;
import app.paysector.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static app.paysector.web.TestBuilder.randomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IndexController.class)
public class IndexControllerApiTest {

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private BillService billService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestIndexEndpoint_returnIndexPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRequestRegisterEndpoint_returnRegisterPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/register");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));

    }

    @Test
    void getRequestToLoginEndpoint_returnLoginPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/login");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"));

    }


    @Test
    void getRequestToLoginEndpointWithErrorParameter_shouldReturnLoginViewAndErrorMessageAttribute() throws Exception {


        MockHttpServletRequestBuilder request = get("/login").param("error", "");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest", "errorMessage"));
    }

    @Test
    void postRequestToRegisterEndpoint_successfulRegister() throws Exception {
        MockHttpServletRequestBuilder request = post("/register")
                .formField("username", "lenkin")
                .formField("password", "asdasd")
                .formField("confirmPassword", "asdasd")
                .formField("firstName", "Borislav")
                .formField("lastName", "Solakov")
                .formField("email", "lenkin@mail.bg")
                .formField("country", "BULGARIA")
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"));
        verify(userService, times(1)).registerUser(any());
    }

    @Test
    void getAuthenticatedRequestToHome_returnsHomeView() throws Exception {

        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/home")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(userId);
    }

    @Test
    void getUnauthenticatedRequestToHome_redirectToLogin() throws Exception {

        MockHttpServletRequestBuilder request = get("/home");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
        verify(userService, never()).getById(any());
    }
}
