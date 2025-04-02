package app.paysector.web;

import app.paysector.loan.model.Loan;
import app.paysector.loan.service.LoanService;
import app.paysector.security.AuthenticateUser;
import app.paysector.user.model.UserRole;
import app.paysector.user.service.UserService;
import app.paysector.wallet.service.WalletService;
import app.paysector.web.dto.LoanRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.math.BigDecimal;
import java.util.UUID;

import static app.paysector.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(LoanController.class)
public class LoanControllerApiTest {
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private LoanService loanService;
    @MockitoBean
    private WalletService walletService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAuthenticatedLoanEndpoint_returnLoanPage() throws Exception {

        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/loan")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("loan"))
                .andExpect(model().attributeExists("loans"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getUnauthenticatedLoanEndpoint_returnRedirectToLoginPage() throws Exception {

        MockHttpServletRequestBuilder request = get("/loan");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void getAuthenticatedRequestLoanEndpoint_returnRequestLoanPage() throws Exception {

        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/loan/request-loan")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("request-loan"))
                .andExpect(model().attributeExists("loanRequest"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getAuthenticatedLoanInfoEndpoint_returnLoanInfoPage() throws Exception {

        when(userService.getById(any())).thenReturn(randomUser());
        when(loanService.getById(any())).thenReturn(randomLoan());

        UUID userId = UUID.randomUUID();
        UUID loanId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/loan/{loanId}/info", loanId)
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("loan-info"))
                .andExpect(model().attributeExists("loan"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getRequestOfferDetailsEndpoint_returnRequestOfferDetailsPage() throws Exception {
        LoanRequest loanRequest = LoanRequest.builder()
                .periodInMonths(3)
                .amount(BigDecimal.valueOf(1000))
                .build();
        when(userService.getById(any())).thenReturn(randomUser());
        when(loanService.getById(any())).thenReturn(randomLoan());
        when(loanService.initializeLoan(any(), any())).thenReturn(randomLoan());


        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/loan/offer-details")
                .flashAttr("loanRequest", loanRequest)
                .with(user(principal));


        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("offer-details"))
                .andExpect(model().attributeExists("loanDetails"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void postAcceptLoanDetails_returnCreatedLoan() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = post("/loan/accept-details")
                .formField("periodInMonths","3")
                .formField("amount","1000")
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/loan"));
    }

    @Test
    void putRequestFullRepaymentLoan_returnSuccessfullyRepaidLoan() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        when(loanService.getById(any())).thenReturn(randomLoan());
        when(loanService.fullRepayment(any(), any())).thenReturn(randomTransaction());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/loan/{loanId}/full-repayment", randomLoan().getId())
                .with(user(principal))
                .with(csrf());

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/transactions"));
    }

    @Test
    void getAuthenticatedRequestRefinanceEndpoint_returnRequestRefinancePage() throws Exception {

        when(userService.getById(any())).thenReturn(randomUser());
        when(loanService.getById(any())).thenReturn(randomLoan());
        when(loanService.getLoansByOwnerId(any()).get(0)).thenReturn(randomLoan());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/loan/request-refinance")
                .with(user(principal));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("request-refinance"))
                .andExpect(model().attributeExists("loanRequest"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("loanToRefinance"));
    }
}
