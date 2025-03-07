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
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "amazon.dynamodb.endpoint=http://localhost:8000/",
        "amazon.aws.accesskey=test",
        "amazon.aws.secretkey=test"
})
public class CustomerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    private AmazonDynamoDB amazonDynamoDB;
    private static final String BASE_URL = "http://localhost:";
    private static final String API_PATH = "/v1/customer";

    @BeforeEach
    public void setup() {
        amazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB();
        CreateTableRequest tableRequest = dynamoDBMapper.generateCreateTableRequest(Customer.class);
        tableRequest.setProvisionedThroughput(new ProvisionedThroughput(1L, 1L));
        amazonDynamoDB.createTable(tableRequest);
    }

    @AfterEach
    public void tearDown() {
        amazonDynamoDB.shutdown();
    }

    @Test
    public void whenCreateCustomer_thenCustomerIsCreated() {
        // Given
        Customer customer = new Customer()
                .withName("John Doe")
                .withAddress("123 Main St")
                .withPhoneNumber("555-1234");

        // When
        ResponseEntity<Customer> response = restTemplate.postForEntity(
                BASE_URL + port + API_PATH,
                customer,
                Customer.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getName()).isEqualTo("John Doe");
        assertThat(response.getBody().getAddress()).isEqualTo("123 Main St");
        assertThat(response.getBody().getPhoneNumber()).isEqualTo("555-1234");
    }

    @Test
    public void whenGetCustomer_thenCustomerIsReturned() {
        // Given
        Customer customer = new Customer()
                .withName("Jane Doe")
                .withAddress("456 Oak St")
                .withPhoneNumber("555-5678");
        customerRepository.save(customer);

        // When
        ResponseEntity<Customer> response = restTemplate.getForEntity(
                BASE_URL + port + API_PATH + "/{name}",
                Customer.class,
                customer.getName());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Jane Doe");
    }

    @Test
    public void whenUpdateCustomer_thenCustomerIsUpdated() {
        // Given
        Customer customer = new Customer()
                .withName("Bob Smith")
                .withAddress("789 Pine St")
                .withPhoneNumber("555-9012");
        customerRepository.save(customer);

        Customer updatedCustomer = new Customer()
                .withAddress("999 New St")
                .withPhoneNumber("555-9999");

        // When
        ResponseEntity<Customer> response = restTemplate.exchange(
                BASE_URL + port + API_PATH + "/{name}",
                HttpMethod.PATCH,
                new HttpEntity<>(updatedCustomer),
                Customer.class,
                customer.getName());

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAddress()).isEqualTo("999 New St");
        assertThat(response.getBody().getPhoneNumber()).isEqualTo("555-9999");
    }

    @Test
    public void whenDeleteCustomer_thenCustomerIsDeleted() {
        // Given
        Customer customer = new Customer()
                .withName("Alice Johnson")
                .withAddress("321 Elm St")
                .withPhoneNumber("555-3456");
        customerRepository.save(customer);

        // When
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                BASE_URL + port + API_PATH + "/{name}",
                HttpMethod.DELETE,
                null,
                Void.class,
                customer.getName());

        // Then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Customer> getResponse = restTemplate.getForEntity(
                BASE_URL + port + API_PATH + "/{name}",
                Customer.class,
                customer.getName());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void whenListCustomers_thenAllCustomersAreReturned() {
        // Given
        Customer customer1 = new Customer()
                .withName("Customer 1")
                .withAddress("Address 1")
                .withPhoneNumber("555-1111");
        Customer customer2 = new Customer()
                .withName("Customer 2")
                .withAddress("Address 2")
                .withPhoneNumber("555-2222");

        customerRepository.save(customer1);
        customerRepository.save(customer2);

        // When
        ResponseEntity<List> response = restTemplate.getForEntity(
                BASE_URL + port + API_PATH,
                List.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(2);
    }
}