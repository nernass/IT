package com.springboot.springbootsecurity.product;

import com.springboot.springbootsecurity.common.model.CustomPage;
import com.springboot.springbootsecurity.common.model.dto.response.CustomPagingResponse;
import com.springboot.springbootsecurity.common.model.dto.response.CustomResponse;
import com.springboot.springbootsecurity.product.controller.ProductController;
import com.springboot.springbootsecurity.product.model.Product;
import com.springboot.springbootsecurity.product.model.dto.request.ProductCreateRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductPagingRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductUpdateRequest;
import com.springboot.springbootsecurity.product.model.dto.response.ProductResponse;
import com.springboot.springbootsecurity.product.model.entity.ProductEntity;
import com.springboot.springbootsecurity.product.model.mapper.CustomPageToCustomPagingResponseMapper;
import com.springboot.springbootsecurity.product.service.ProductCreateService;
import com.springboot.springbootsecurity.product.service.ProductDeleteService;
import com.springboot.springbootsecurity.product.service.ProductReadService;
import com.springboot.springbootsecurity.product.service.ProductUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceIntegrationTest {

    @Mock
    private ProductCreateService productCreateService;

    @Mock
    private ProductReadService productReadService;

    @Mock
    private ProductUpdateService productUpdateService;

    @Mock
    private ProductDeleteService productDeleteService;

    private ProductController productController;

    @BeforeEach
    public void setup() {
        productController = new ProductController(
                productCreateService,
                productReadService,
                productUpdateService,
                productDeleteService);
    }

    @Test
    public void testCreateProduct_Success() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";
        String productName = "Test Product";
        BigDecimal amount = new BigDecimal("10.0000");
        BigDecimal unitPrice = new BigDecimal("25.0000");

        ProductCreateRequest request = ProductCreateRequest.builder()
                .name(productName)
                .amount(amount)
                .unitPrice(unitPrice)
                .build();

        Product createdProduct = Product.builder()
                .id(productId)
                .name(productName)
                .amount(amount)
                .unitPrice(unitPrice)
                .build();

        when(productCreateService.createProduct(any(ProductCreateRequest.class))).thenReturn(createdProduct);

        // Act
        CustomResponse<String> response = productController.createProduct(request);

        // Assert
        verify(productCreateService).createProduct(request);
        assertEquals(productId, response.getData());
        assertTrue(response.isSuccess());
    }

    @Test
    public void testGetProductById_Success() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";
        String productName = "Test Product";
        BigDecimal amount = new BigDecimal("10.0000");
        BigDecimal unitPrice = new BigDecimal("25.0000");

        Product product = Product.builder()
                .id(productId)
                .name(productName)
                .amount(amount)
                .unitPrice(unitPrice)
                .build();

        when(productReadService.getProductById(productId)).thenReturn(product);

        // Act
        CustomResponse<ProductResponse> response = productController.getProductById(productId);

        // Assert
        verify(productReadService).getProductById(productId);
        assertEquals(productId, response.getData().getId());
        assertEquals(productName, response.getData().getName());
        assertEquals(amount, response.getData().getAmount());
        assertEquals(unitPrice, response.getData().getUnitPrice());
        assertTrue(response.isSuccess());
    }

    @Test
    public void testGetProducts_Success() {
        // Arrange
        ProductPagingRequest pagingRequest = new ProductPagingRequest();

        Product product1 = Product.builder()
                .id("id1")
                .name("Product 1")
                .amount(new BigDecimal("10.0000"))
                .unitPrice(new BigDecimal("20.0000"))
                .build();

        Product product2 = Product.builder()
                .id("id2")
                .name("Product 2")
                .amount(new BigDecimal("15.0000"))
                .unitPrice(new BigDecimal("30.0000"))
                .build();

        List<Product> products = Arrays.asList(product1, product2);
        CustomPage<Product> page = new CustomPage<>(
                products,
                2,
                0,
                10,
                10,
                1);

        when(productReadService.getProducts(pagingRequest)).thenReturn(page);

        // Act
        CustomResponse<CustomPagingResponse<ProductResponse>> response = productController.getProducts(pagingRequest);

        // Assert
        verify(productReadService).getProducts(pagingRequest);
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().getTotalElements());
        assertEquals(2, response.getData().getContent().size());
        assertEquals("id1", response.getData().getContent().get(0).getId());
        assertEquals("id2", response.getData().getContent().get(1).getId());
    }

    @Test
    public void testUpdateProductById_Success() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";
        String updatedName = "Updated Product";
        BigDecimal updatedAmount = new BigDecimal("20.0000");
        BigDecimal updatedUnitPrice = new BigDecimal("35.0000");

        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name(updatedName)
                .amount(updatedAmount)
                .unitPrice(updatedUnitPrice)
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name(updatedName)
                .amount(updatedAmount)
                .unitPrice(updatedUnitPrice)
                .build();

        when(productUpdateService.updateProductById(eq(productId), any(ProductUpdateRequest.class)))
                .thenReturn(updatedProduct);

        // Act
        CustomResponse<ProductResponse> response = productController.updatedProductById(updateRequest, productId);

        // Assert
        verify(productUpdateService).updateProductById(productId, updateRequest);
        assertTrue(response.isSuccess());
        assertEquals(productId, response.getData().getId());
        assertEquals(updatedName, response.getData().getName());
        assertEquals(updatedAmount, response.getData().getAmount());
        assertEquals(updatedUnitPrice, response.getData().getUnitPrice());
    }

    @Test
    public void testDeleteProductById_Success() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";

        // Act
        CustomResponse<Void> response = productController.deleteProductById(productId);

        // Assert
        verify(productDeleteService).deleteProductById(productId);
        assertTrue(response.isSuccess());
    }

    @Test
    public void testGetProductById_NotFound() {
        // Arrange
        String productId = "non-existent-id";

        when(productReadService.getProductById(productId))
                .thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            productController.getProductById(productId);
        });

        verify(productReadService).getProductById(productId);
    }

    @Test
    public void testUpdateProduct_InvalidData() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name("Updated Product")
                .amount(new BigDecimal("20.0000"))
                .unitPrice(new BigDecimal("35.0000"))
                .build();

        when(productUpdateService.updateProductById(eq(productId), any(ProductUpdateRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid product data"));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productController.updatedProductById(updateRequest, productId);
        });

        assertEquals("Invalid product data", exception.getMessage());
        verify(productUpdateService).updateProductById(productId, updateRequest);
    }

    @Test
    public void testCreateAndGetProduct_IntegrationFlow() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";
        String productName = "Integration Test Product";
        BigDecimal amount = new BigDecimal("10.0000");
        BigDecimal unitPrice = new BigDecimal("25.0000");

        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .name(productName)
                .amount(amount)
                .unitPrice(unitPrice)
                .build();

        Product createdProduct = Product.builder()
                .id(productId)
                .name(productName)
                .amount(amount)
                .unitPrice(unitPrice)
                .build();

        // Mock create service
        when(productCreateService.createProduct(any(ProductCreateRequest.class))).thenReturn(createdProduct);

        // Mock read service to return the same product
        when(productReadService.getProductById(productId)).thenReturn(createdProduct);

        // Act - Create and then retrieve the product
        CustomResponse<String> createResponse = productController.createProduct(createRequest);
        CustomResponse<ProductResponse> getResponse = productController.getProductById(productId);

        // Assert
        // Verify the create service was called with the correct request
        ArgumentCaptor<ProductCreateRequest> createRequestCaptor = ArgumentCaptor.forClass(ProductCreateRequest.class);
        verify(productCreateService).createProduct(createRequestCaptor.capture());
        assertEquals(productName, createRequestCaptor.getValue().getName());

        // Verify the read service was called with the correct ID
        verify(productReadService).getProductById(productId);

        // Verify responses
        assertEquals(productId, createResponse.getData());
        assertEquals(productId, getResponse.getData().getId());
        assertEquals(productName, getResponse.getData().getName());
        assertEquals(amount, getResponse.getData().getAmount());
        assertEquals(unitPrice, getResponse.getData().getUnitPrice());
    }

    @Test
    public void testCreateUpdateAndGetProduct_IntegrationFlow() {
        // Arrange
        String productId = "123e4567-e89b-12d3-a456-426614174000";

        // Initial product data
        String initialName = "Initial Product";
        BigDecimal initialAmount = new BigDecimal("10.0000");
        BigDecimal initialUnitPrice = new BigDecimal("25.0000");

        // Updated product data
        String updatedName = "Updated Product";
        BigDecimal updatedAmount = new BigDecimal("20.0000");
        BigDecimal updatedUnitPrice = new BigDecimal("35.0000");

        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .name(initialName)
                .amount(initialAmount)
                .unitPrice(initialUnitPrice)
                .build();

        Product createdProduct = Product.builder()
                .id(productId)
                .name(initialName)
                .amount(initialAmount)
                .unitPrice(initialUnitPrice)
                .build();

        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
                .name(updatedName)
                .amount(updatedAmount)
                .unitPrice(updatedUnitPrice)
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name(updatedName)
                .amount(updatedAmount)
                .unitPrice(updatedUnitPrice)
                .build();

        // Mock services
        when(productCreateService.createProduct(any(ProductCreateRequest.class))).thenReturn(createdProduct);
        when(productUpdateService.updateProductById(eq(productId), any(ProductUpdateRequest.class)))
                .thenReturn(updatedProduct);
        when(productReadService.getProductById(productId)).thenReturn(updatedProduct);

        // Act - Create, update, and then retrieve the product
        productController.createProduct(createRequest);
        productController.updatedProductById(updateRequest, productId);
        CustomResponse<ProductResponse> getResponse = productController.getProductById(productId);

        // Assert
        verify(productCreateService).createProduct(any(ProductCreateRequest.class));
        verify(productUpdateService).updateProductById(eq(productId), any(ProductUpdateRequest.class));
        verify(productReadService).getProductById(productId);

        // Verify the final product state
        assertEquals(productId, getResponse.getData().getId());
        assertEquals(updatedName, getResponse.getData().getName());
        assertEquals(updatedAmount, getResponse.getData().getAmount());
        assertEquals(updatedUnitPrice, getResponse.getData().getUnitPrice());
    }
}