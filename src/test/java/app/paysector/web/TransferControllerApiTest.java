package app.paysector.web;

import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.User;
import app.paysector.user.model.UserRole;
import app.paysector.user.service.UserService;
import app.paysector.wallet.model.Wallet;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;


import java.util.UUID;

import static app.paysector.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(TransferController.class)
public class TransferControllerApiTest {
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WalletService walletService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void getRequestTransferEndpoint_returnTransferPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/transfers")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("transfer"))
                .andExpect(model().attribute("transferRequest", new TransferRequest()))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void postRequestTransferEndpoint_returnSuccessfulTransfer() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        when(walletService.transferFunds(any(), any())).thenReturn(randomTransaction());


        User user = randomUser();
        Wallet wallet = randomWallet();
        user.setWallet(wallet);

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = post("/transfers")
                .formField("receiverUsername", user.getUsername())
                .formField("amount", "100")
                .formField("description", "")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/transactions"));
        verify(userService, times(1)).getById(any());
        verify(walletService, times(1)).transferFunds(any(), any());
    }
}
