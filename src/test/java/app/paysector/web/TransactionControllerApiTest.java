package app.paysector.web;

import app.paysector.security.AuthenticateUser;
import app.paysector.transaction.model.Transaction;
import app.paysector.transaction.service.TransactionService;
import app.paysector.user.model.UserRole;
import app.paysector.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.UUID;

import static app.paysector.web.TestBuilder.randomTransaction;
import static app.paysector.web.TestBuilder.randomUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
public class TransactionControllerApiTest {
    @MockitoBean
    private TransactionService transactionService;
    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getRequestTransactionsEndpoint_returnTransactionsPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        List<Transaction> transactions = List.of(randomTransaction(), randomTransaction());
        when(transactionService.findByOwnerId(any())).thenReturn(transactions);

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/transactions")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("user"));
        verify(userService, times(1)).getById(any());
        verify(transactionService, times(1)).findByOwnerId(any());
    }
}
