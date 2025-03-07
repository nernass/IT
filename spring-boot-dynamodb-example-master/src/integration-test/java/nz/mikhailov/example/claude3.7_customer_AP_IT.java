package nz.mikhailov.example.customer;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer testCustomer;
    private Customer updatedCustomer;

    @BeforeEach
    void setUp() {
        // Setup test customer data
        testCustomer = new Customer()
                .withName("John Doe")
                .withAddress("123 Test St")
                .withPhoneNumber("555-1234");

        updatedCustomer = new Customer()
                .withName("John Doe")
                .withAddress("456 New St")
                .withPhoneNumber("555-5678");

        // Reset mocks before each test
        reset(dynamoDBMapper);
    }

    @Test
    public void testCreateCustomerSuccess() throws Exception {
        // Mock repository behavior - customer doesn't exist yet
        when(dynamoDBMapper.load(Customer.class, "John Doe")).thenReturn(null);

        // Execute and verify HTTP request/response
        mockMvc.perform(post("/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"address\":\"123 Test St\",\"phoneNumber\":\"555-1234\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.address", is("123 Test St")))
                .andExpect(jsonPath("$.phoneNumber", is("555-1234")));

        // Verify repository was called correctly
        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(dynamoDBMapper).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("John Doe", savedCustomer.getName());
        assertEquals("123 Test St", savedCustomer.getAddress());
        assertEquals("555-1234", savedCustomer.getPhoneNumber());
    }

    @Test
    public void testCreateCustomerConflict() throws Exception {
        // Mock repository behavior - customer already exists
        when(dynamoDBMapper.load(Customer.class, "John Doe")).thenReturn(testCustomer);

        // Execute and verify HTTP request/response
        mockMvc.perform(post("/v1/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"John Doe\",\"address\":\"123 Test St\",\"phoneNumber\":\"555-1234\"}"))
                .andExpect(status().isConflict());

        // Verify repository was called correctly but save wasn't
        verify(dynamoDBMapper).load(Customer.class, "John Doe");
        verify(dynamoDBMapper, never()).save(any(Customer.class));
    }

    @Test
    public void testGetCustomerSuccess() throws Exception {
        // Mock repository behavior
        when(dynamoDBMapper.load(Customer.class, "John Doe")).thenReturn(testCustomer);

        // Execute and verify HTTP request/response
        mockMvc.perform(get("/v1/customer/John Doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.address", is("123 Test St")))
                .andExpect(jsonPath("$.phoneNumber", is("555-1234")));

        // Verify repository was called correctly
        verify(dynamoDBMapper).load(Customer.class, "John Doe");
    }

    @Test
    public void testGetCustomerNotFound() throws Exception {
        // Mock repository behavior
        when(dynamoDBMapper.load(Customer.class, "Unknown")).thenReturn(null);

        // Execute and verify HTTP request/response
        mockMvc.perform(get("/v1/customer/Unknown"))
                .andExpect(status().isNotFound());

        // Verify repository was called correctly
        verify(dynamoDBMapper).load(Customer.class, "Unknown");
    }

    @Test
    public void testUpdateCustomerSuccess() throws Exception {
        // Mock repository behavior
        when(dynamoDBMapper.load(Customer.class, "John Doe")).thenReturn(testCustomer);

        // Execute and verify HTTP request/response
        mockMvc.perform(put("/v1/customer/John Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"address\":\"456 New St\",\"phoneNumber\":\"555-5678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.address", is("456 New St")))
                .andExpect(jsonPath("$.phoneNumber", is("555-5678")));

        // Verify repository was called correctly
        verify(dynamoDBMapper).load(Customer.class, "John Doe");

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(dynamoDBMapper).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("John Doe", savedCustomer.getName());
        assertEquals("456 New St", savedCustomer.getAddress());
        assertEquals("555-5678", savedCustomer.getPhoneNumber());
    }

    @Test
    public void testPatchCustomerSuccess() throws Exception {
        // Mock repository behavior
        when(dynamoDBMapper.load(Customer.class, "John Doe")).thenReturn(testCustomer);

        // Execute and verify HTTP request/response - only changing address
        mockMvc.perform(patch("/v1/customer/John Doe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"address\":\"456 New St\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.address", is("456 New St")))
                .andExpect(jsonPath("$.phoneNumber", is("555-1234"))); // Should remain unchanged

        // Verify repository was called correctly
        verify(dynamoDBMapper).load(Customer.class, "John Doe");

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(dynamoDBMapper).save(customerCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("John Doe", savedCustomer.getName());
        assertEquals("456 New St", savedCustomer.getAddress());
        assertEquals("555-1234", savedCustomer.getPhoneNumber());
    }

    @Test
    public void testDeleteCustomerSuccess() throws Exception {
        // Mock repository behavior
        when(dynamoDBMapper.load(Customer.class, "John Doe")).thenReturn(testCustomer);

        // Execute and verify HTTP request/response
        mockMvc.perform(delete("/v1/customer/John Doe"))
                .andExpect(status().isNoContent());

        // Verify repository was called correctly
        verify(dynamoDBMapper).load(Customer.class, "John Doe");
        verify(dynamoDBMapper).delete(any(Customer.class), any());
    }

    @Test
    public void testListCustomersSuccess() throws Exception {
        // Create test data
        List<Customer> customers = Arrays.asList(
                testCustomer,
                new Customer().withName("Jane Smith").withAddress("789 Other St").withPhoneNumber("555-9876"));

        // Mock repository behavior
        @SuppressWarnings("unchecked")
        PaginatedScanList<Customer> mockScanList = mock(PaginatedScanList.class);
        when(mockScanList.iterator()).thenReturn(customers.iterator());
        when(dynamoDBMapper.scan(eq(Customer.class), any())).thenReturn(mockScanList);

        // Execute and verify HTTP request/response
        mockMvc.perform(get("/v1/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));

        // Verify repository was called correctly
        verify(dynamoDBMapper).scan(eq(Customer.class), any());
    }

    @Test
    public void testListCustomersEmpty() throws Exception {
        // Mock repository behavior for empty list
        @SuppressWarnings("unchecked")
        PaginatedScanList<Customer> mockScanList = mock(PaginatedScanList.class);
        when(mockScanList.iterator()).thenReturn(new ArrayList<Customer>().iterator());
        when(dynamoDBMapper.scan(eq(Customer.class), any())).thenReturn(mockScanList);

        // Execute and verify HTTP request/response
        mockMvc.perform(get("/v1/customer"))
                .andExpect(status().isNoContent());

        // Verify repository was called correctly
        verify(dynamoDBMapper).scan(eq(Customer.class), any());
    }
}