package nz.mikhailov.example.customer;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest
public class CustomerIntegrationTest {

    @Autowired
    private CustomerController customerController;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @MockBean
    private DynamoDBMapper dynamoDBMapper;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer()
                .withName("Test Customer")
                .withAddress("123 Test St")
                .withPhoneNumber("555-0123");
    }

    @Test
    void testSuccessfulCustomerCreationFlow() {
        // Mock DynamoDB responses
        when(dynamoDBMapper.load(Customer.class, testCustomer.getName())).thenReturn(null);
        doNothing().when(dynamoDBMapper).save(any(Customer.class));

        // Test creation through controller
        ResponseEntity<Customer> response = customerController.create(testCustomer);

        // Verify response
        assertEquals(CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testCustomer.getName(), response.getBody().getName());

        // Verify interactions
        verify(dynamoDBMapper).load(Customer.class, testCustomer.getName());
        verify(dynamoDBMapper).save(any(Customer.class));
    }

    @Test
    void testCustomerRetrievalFlow() {
        // Mock DynamoDB response
        when(dynamoDBMapper.load(Customer.class, testCustomer.getName())).thenReturn(testCustomer);

        // Test retrieval through controller
        ResponseEntity<Customer> response = customerController.read(testCustomer.getName());

        // Verify response
        assertEquals(OK, response.getStatusCode());
        assertEquals(testCustomer, response.getBody());

        // Verify interactions
        verify(dynamoDBMapper).load(Customer.class, testCustomer.getName());
    }

    @Test
    void testCustomerUpdateFlow() {
        // Mock DynamoDB responses
        when(dynamoDBMapper.load(Customer.class, testCustomer.getName())).thenReturn(testCustomer);
        doNothing().when(dynamoDBMapper).save(any(Customer.class));

        // Create update data
        Customer updateData = new Customer()
                .withName(testCustomer.getName())
                .withAddress("New Address");

        // Test update through controller
        ResponseEntity<Customer> response = customerController.patch(testCustomer.getName(), updateData);

        // Verify response
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Address", response.getBody().getAddress());

        // Verify interactions
        verify(dynamoDBMapper).load(Customer.class, testCustomer.getName());
        verify(dynamoDBMapper).save(any(Customer.class));
    }

    @Test
    void testCustomerListFlow() {
        // Mock DynamoDB response
        List<Customer> customerList = Arrays.asList(testCustomer);
        when(dynamoDBMapper.scan(eq(Customer.class), any())).thenReturn(customerList);

        // Test listing through controller
        ResponseEntity<List<Customer>> response = customerController.list();

        // Verify response
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        // Verify interactions
        verify(dynamoDBMapper).scan(eq(Customer.class), any());
    }

    @Test
    void testCustomerDeletionFlow() {
        // Mock DynamoDB responses
        when(dynamoDBMapper.load(Customer.class, testCustomer.getName())).thenReturn(testCustomer);
        doNothing().when(dynamoDBMapper).delete(any(Customer.class), any());

        // Test deletion through controller
        ResponseEntity<Void> response = customerController.delete(testCustomer.getName());

        // Verify response
        assertEquals(NO_CONTENT, response.getStatusCode());

        // Verify interactions
        verify(dynamoDBMapper).load(Customer.class, testCustomer.getName());
        verify(dynamoDBMapper).delete(any(Customer.class), any());
    }

    @Test
    void testCustomerCreationFailureFlow() {
        // Mock DynamoDB to simulate existing customer
        when(dynamoDBMapper.load(Customer.class, testCustomer.getName())).thenReturn(testCustomer);

        // Test creation through controller
        ResponseEntity<Customer> response = customerController.create(testCustomer);

        // Verify response
        assertEquals(CONFLICT, response.getStatusCode());

        // Verify interactions
        verify(dynamoDBMapper).load(Customer.class, testCustomer.getName());
        verify(dynamoDBMapper, never()).save(any(Customer.class));
    }

    @Test
    void testCustomerNotFoundFlow() {
        // Mock DynamoDB to return null (customer not found)
        when(dynamoDBMapper.load(Customer.class, testCustomer.getName())).thenReturn(null);

        // Test retrieval through controller
        ResponseEntity<Customer> response = customerController.read(testCustomer.getName());

        // Verify response
        assertEquals(NOT_FOUND, response.getStatusCode());

        // Verify interactions
        verify(dynamoDBMapper).load(Customer.class, testCustomer.getName());
    }
}