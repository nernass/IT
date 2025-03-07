package nz.mikhailov.example.customer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import nz.mikhailov.example.Application;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CustomerIntegrationTest {

    private static AmazonDynamoDB dynamoDB;
    private static DynamoDBMapper dynamoDBMapper;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    private String baseUrl;

    @DynamicPropertySource
    static void dynamoDbProperties(DynamicPropertyRegistry registry) {
        // Configure DynamoDB client to use local instance
        dynamoDB = DynamoDBEmbedded.create().amazonDynamoDB();
        registry.add("amazon.dynamodb.endpoint", () -> "http://localhost:8000");
        registry.add("amazon.aws.accesskey", () -> "test");
        registry.add("amazon.aws.secretkey", () -> "test");
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/v1";

        // Create DynamoDB mapper
        dynamoDBMapper = new DynamoDBMapper(dynamoDB);

        // Create Customer table
        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Customer.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        dynamoDB.createTable(tableRequest);

        // Add test data
        Customer customer = new Customer()
                .withName("John Doe")
                .withAddress("123 Main St")
                .withPhoneNumber("555-1234");
        customerRepository.save(customer);
    }

    @AfterEach
    void tearDown() {
        // Clean up the database after each test
        dynamoDB.deleteTable("Customer");
    }

    @Test
    void testGetAllCustomers() {
        ResponseEntity<Customer[]> response = restTemplate.getForEntity(
                baseUrl + "/customer", Customer[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    void testGetCustomerByName() {
        ResponseEntity<Customer> response = restTemplate.getForEntity(
                baseUrl + "/customer/John Doe", Customer.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("123 Main St", response.getBody().getAddress());
        assertEquals("555-1234", response.getBody().getPhoneNumber());
    }

    @Test
    void testGetNonExistentCustomer() {
        ResponseEntity<Customer> response = restTemplate.getForEntity(
                baseUrl + "/customer/Nobody", Customer.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateCustomer() {
        Customer newCustomer = new Customer()
                .withName("Jane Smith")
                .withAddress("456 Oak Ave")
                .withPhoneNumber("555-5678");

        ResponseEntity<Customer> response = restTemplate.postForEntity(
                baseUrl + "/customer", newCustomer, Customer.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verify customer was created
        ResponseEntity<Customer> getResponse = restTemplate.getForEntity(
                baseUrl + "/customer/Jane Smith", Customer.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals("Jane Smith", getResponse.getBody().getName());
        assertEquals("456 Oak Ave", getResponse.getBody().getAddress());
    }

    @Test
    void testCreateExistingCustomer() {
        Customer existingCustomer = new Customer()
                .withName("John Doe")
                .withAddress("Another Address")
                .withPhoneNumber("555-9876");

        ResponseEntity<Customer> response = restTemplate.postForEntity(
                baseUrl + "/customer", existingCustomer, Customer.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testReplaceCustomer() {
        Customer updatedCustomer = new Customer()
                .withAddress("789 New Ave")
                .withPhoneNumber("555-8765");

        HttpEntity<Customer> requestEntity = new HttpEntity<>(updatedCustomer);

        ResponseEntity<Customer> response = restTemplate.exchange(
                baseUrl + "/customer/John Doe",
                HttpMethod.PUT,
                requestEntity,
                Customer.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("789 New Ave", response.getBody().getAddress());
        assertEquals("555-8765", response.getBody().getPhoneNumber());
    }

    @Test
    void testPartialUpdateCustomer() {
        Customer partialUpdate = new Customer()
                .withPhoneNumber("555-9999");

        HttpEntity<Customer> requestEntity = new HttpEntity<>(partialUpdate);

        ResponseEntity<Customer> response = restTemplate.exchange(
                baseUrl + "/customer/John Doe",
                HttpMethod.PATCH,
                requestEntity,
                Customer.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        assertEquals("123 Main St", response.getBody().getAddress()); // Should remain unchanged
        assertEquals("555-9999", response.getBody().getPhoneNumber()); // Should be updated
    }

    @Test
    void testDeleteCustomer() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/customer/John Doe",
                HttpMethod.DELETE,
                null,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify deletion
        ResponseEntity<Customer> getResponse = restTemplate.getForEntity(
                baseUrl + "/customer/John Doe", Customer.class);

        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }

    @Test
    void testDeleteNonexistentCustomer() {
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/customer/Nobody",
                HttpMethod.DELETE,
                null,
                Void.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}