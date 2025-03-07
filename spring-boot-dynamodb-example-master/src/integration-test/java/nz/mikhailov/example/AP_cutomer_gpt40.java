package nz.mikhailov.example.customer;

import nz.mikhailov.example.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    public void testListCustomersSuccess() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St").withPhoneNumber("555-1234");
        when(customerService.list()).thenReturn(Arrays.asList(customer));

        mockMvc.perform(get("/v1/customer")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].address").value("123 Main St"))
                .andExpect(jsonPath("$[0].phoneNumber").value("555-1234"));
    }

    @Test
    public void testReadCustomerSuccess() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St").withPhoneNumber("555-1234");
        when(customerService.read(anyString())).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/v1/customer/John Doe")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.phoneNumber").value("555-1234"));
    }

    @Test
    public void testCreateCustomerSuccess() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St").withPhoneNumber("555-1234");
        when(customerService.create(any(Customer.class))).thenReturn(Optional.of(customer));

        mockMvc.perform(post("/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"address\":\"123 Main St\",\"phoneNumber\":\"555-1234\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.phoneNumber").value("555-1234"));
    }

    @Test
    public void testUpdateCustomerSuccess() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St").withPhoneNumber("555-1234");
        when(customerService.update(any(Customer.class))).thenReturn(Optional.of(customer));

        mockMvc.perform(patch("/v1/customer/John Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"address\":\"456 Elm St\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.address").value("456 Elm St"))
                .andExpect(jsonPath("$.phoneNumber").value("555-1234"));
    }

    @Test
    public void testDeleteCustomerSuccess() throws Exception {
        when(customerService.delete(anyString())).thenReturn(true);

        mockMvc.perform(delete("/v1/customer/John Doe")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testReadCustomerNotFound() throws Exception {
        when(customerService.read(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/customer/Unknown")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateCustomerConflict() throws Exception {
        when(customerService.create(any(Customer.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"address\":\"123 Main St\",\"phoneNumber\":\"555-1234\"}"))
                .andExpect(status().isConflict());
    }
}