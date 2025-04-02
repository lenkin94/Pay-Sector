package app.paysector.web;

import app.paysector.bill.service.BillService;
import app.paysector.loan.service.LoanService;
import app.paysector.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    void requestToIndexEndpoint_returnIndexPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void requestToLoginEndpoint_returnLoginPage() throws Exception {
        MockHttpServletRequestBuilder request = get("/login");



    }
}
