package com.springboot.springbootsecurity.product.controller;

import com.springboot.springbootsecurity.product.model.Product;
import com.springboot.springbootsecurity.product.model.dto.request.ProductCreateRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductUpdateRequest;
import com.springboot.springbootsecurity.product.model.dto.response.ProductResponse;
import com.springboot.springbootsecurity.product.service.ProductCreateService;
import com.springboot.springbootsecurity.product.service.ProductDeleteService;
import com.springboot.springbootsecurity.product.service.ProductReadService;
import com.springboot.springbootsecurity.product.service.ProductUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ProductControllerIntegrationTest {

    @Mock
    private ProductCreateService productCreateService;

    @Mock
    private ProductReadService productReadService;

    @Mock
    private ProductUpdateService productUpdateService;

    @Mock
    private ProductDeleteService productDeleteService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    public void testCreateProduct_Success() throws Exception {
        ProductCreateRequest request = new ProductCreateRequest("Product1", new BigDecimal("10.00"),
                new BigDecimal("5.00"));
        Product product = new Product("1", "Product1", new BigDecimal("10.00"), new BigDecimal("5.00"));

        when(productCreateService.createProduct(any(ProductCreateRequest.class))).thenReturn(product);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Product1\",\"amount\":10.00,\"unitPrice\":5.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("1"));
    }

    @Test
    public void testGetProductById_Success() throws Exception {
        Product product = new Product("1", "Product1", new BigDecimal("10.00"), new BigDecimal("5.00"));
        ProductResponse response = new ProductResponse("1", "Product1", new BigDecimal("10.00"),
                new BigDecimal("5.00"));

        when(productReadService.getProductById(anyString())).thenReturn(product);
        when(productController.productToProductResponseMapper.map(any(Product.class))).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("Product1"));
    }

    @Test
    public void testUpdateProductById_Success() throws Exception {
        ProductUpdateRequest request = new ProductUpdateRequest("Product1", new BigDecimal("10.00"),
                new BigDecimal("5.00"));
        Product product = new Product("1", "Product1", new BigDecimal("10.00"), new BigDecimal("5.00"));
        ProductResponse response = new ProductResponse("1", "Product1", new BigDecimal("10.00"),
                new BigDecimal("5.00"));

        when(productUpdateService.updateProductById(anyString(), any(ProductUpdateRequest.class))).thenReturn(product);
        when(productController.productToProductResponseMapper.map(any(Product.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Product1\",\"amount\":10.00,\"unitPrice\":5.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.name").value("Product1"));
    }

    @Test
    public void testDeleteProductById_Success() throws Exception {
        doNothing().when(productDeleteService).deleteProductById(anyString());

        mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}