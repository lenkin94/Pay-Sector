package app.paysector.web;

import app.paysector.bill.dto.Bill;
import app.paysector.bill.dto.BillType;
import app.paysector.bill.service.BillService;
import app.paysector.security.AuthenticateUser;
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

import static app.paysector.web.TestBuilder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillController.class)
public class BillControllerApiTest {

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private BillService billService;

    @Autowired
    private MockMvc mvc;

    @Test
    void getRequestBillsPageEndpoint_returnBillsPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        List<Bill> bills = List.of(randomBill(), randomBill());
        when(billService.allUserBills(any())).thenReturn(bills);

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/bills")
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("bills"))
                .andExpect(model().attributeExists("bills"))
                .andExpect(model().attributeExists("user"));
        verify(billService, times(1)).allUserBills(any());
        verify(userService, times(1)).getById(any());
    }

    @Test
    void putRequestPayBill_returnSuccessfullyPaidBill() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());
        when(billService.payBill(any(), any())).thenReturn(randomTransaction());

        UUID userId = UUID.randomUUID();

        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = put("/bills/" + randomBill().getId() +  "/pay")
                .with(user(principal))
                .with(csrf());


        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bills"));

        verify(billService, times(1)).payBill(any(), any());
    }

    @Test
    void getRequestAddBillEndpoint_returnAddBillPage() throws Exception {
        when(userService.getById(any())).thenReturn(randomUser());

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = get("/bills/add-bill")
                .with(user(principal));

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("add-bill"))
                .andExpect(model().attributeExists("bill"))
                .andExpect(model().attributeExists("user"));

        verify(userService, times(1)).getById(any());
    }

    @Test
    void postRequestAddBillEndpoint_returnSuccessfullyAddedBill() throws Exception {

        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = post("/bills/add-bill")
                .formField("billNumber", "12312312")
                .formField("billType", String.valueOf(BillType.ELECTRICITY))
                .with(user(principal))
                .with(csrf());



        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bills"));

        verify(billService, times(1)).addBill(any(), any());
    }

    @Test
    void deleteRequestBillRemoveEndpoint_returnSuccessfullyRemovedBill() throws Exception {

        UUID billId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuthenticateUser principal = new AuthenticateUser(userId, "lenkin", "asdasd", UserRole.USER, true);
        MockHttpServletRequestBuilder request = delete("/bills/{userId}/{billId}/remove", userId, billId)
                .with(user(principal))
                .with(csrf());

        mvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/bills"));

        verify(billService, times(1)).removeBill(any(), any());
    }
}
