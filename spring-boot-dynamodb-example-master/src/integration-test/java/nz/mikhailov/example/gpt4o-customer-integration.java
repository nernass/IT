package nz.mikhailov.example.customer;

import nz.mikhailov.example.Application;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
public class CustomerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private CustomerRepository repository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void testCreateCustomer() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St")
                .withPhoneNumber("1234567890");
        Mockito.when(repository.read(anyString())).thenReturn(Optional.empty());
        Mockito.when(repository.save(any(Customer.class))).thenReturn(customer);

        mockMvc.perform(post("/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"address\":\"123 Main St\",\"phoneNumber\":\"1234567890\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testReadCustomer() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St")
                .withPhoneNumber("1234567890");
        Mockito.when(repository.read("John Doe")).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/v1/customer/John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St")
                .withPhoneNumber("1234567890");
        Mockito.when(repository.read("John Doe")).thenReturn(Optional.of(customer));
        Mockito.when(repository.save(any(Customer.class))).thenReturn(customer.withAddress("456 Elm St"));

        mockMvc.perform(patch("/v1/customer/John Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"address\":\"456 Elm St\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("456 Elm St"));
    }

    @Test
    public void testDeleteCustomer() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St")
                .withPhoneNumber("1234567890");
        Mockito.when(repository.read("John Doe")).thenReturn(Optional.of(customer));
        Mockito.doNothing().when(repository).delete("John Doe");

        mockMvc.perform(delete("/v1/customer/John Doe"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testListCustomers() throws Exception {
        Customer customer = new Customer().withName("John Doe").withAddress("123 Main St")
                .withPhoneNumber("1234567890");
        Mockito.when(repository.readAll()).thenReturn(Collections.singletonList(customer));

        mockMvc.perform(get("/v1/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }
}