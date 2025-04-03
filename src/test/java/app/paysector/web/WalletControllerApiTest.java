package app.paysector.web;

import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.UserRole;
import app.paysector.user.service.UserService;
import app.paysector.wallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.UUID;

import static app.paysector.web.TestBuilder.randomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
public class WalletControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestWalletEndpoint_returnWalletPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/wallet")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("wallet"));
        verify(userService, times(1)).getById(any());
    }

    @Test
    void getRequestAddFundsEndpoint_returnAddFundsPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/wallet/add-funds")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-funds"))
                .andExpect(model().attributeExists("addFundsRequest"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(any());

    }

    @Test
    void putRequestAddFundsEndpoint_returnSuccessfullyAddFunds() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/wallet/add-funds")
                .formField("cardNumber", "1231231231231231")
                .formField("ownerName", "Borislav Solakov")
                .formField("expireDate", LocalDate.now().toString())
                .formField("cvv", "123")
                .formField("amount", "1000")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/wallet"));
        verify(walletService, times(1)).addFunds(any(), any());
    }
}
