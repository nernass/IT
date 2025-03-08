package com.springboot.springbootsecurity.product.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springbootsecurity.common.model.CustomPage;
import com.springboot.springbootsecurity.product.controller.ProductController;
import com.springboot.springbootsecurity.product.model.Product;
import com.springboot.springbootsecurity.product.model.dto.request.ProductCreateRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductPagingRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductUpdateRequest;
import com.springboot.springbootsecurity.product.model.entity.ProductEntity;
import com.springboot.springbootsecurity.product.repository.ProductRepository;
import com.springboot.springbootsecurity.product.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCreateService productCreateService;

    @Autowired
    private ProductReadService productReadService;

    @Autowired
    private ProductUpdateService productUpdateService;

    @Autowired
    private ProductDeleteService productDeleteService;

    private ProductCreateRequest productCreateRequest;
    private ProductUpdateRequest productUpdateRequest;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productCreateRequest = ProductCreateRequest.builder()
                .name("Test Product")
                .amount(new BigDecimal("10.0000"))
                .unitPrice(new BigDecimal("99.9900"))
                .build();

        productUpdateRequest = ProductUpdateRequest.builder()
                .name("Updated Product")
                .amount(new BigDecimal("20.0000"))
                .unitPrice(new BigDecimal("199.9900"))
                .build();

        testProduct = Product.builder()
                .name("Test Product")
                .amount(new BigDecimal("10.0000"))
                .unitPrice(new BigDecimal("99.9900"))
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createProduct_Success() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNotEmpty());
    }

    @Test
    @WithMockUser(authorities = { "ADMIN", "USER" })
    void getProduct_Success() throws Exception {
        Product created = productCreateService.createProduct(productCreateRequest);

        mockMvc.perform(get("/api/v1/products/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(created.getName()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateProduct_Success() throws Exception {
        Product created = productCreateService.createProduct(productCreateRequest);

        mockMvc.perform(put("/api/v1/products/{id}", created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value(productUpdateRequest.getName()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteProduct_Success() throws Exception {
        Product created = productCreateService.createProduct(productCreateRequest);

        mockMvc.perform(delete("/api/v1/products/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(productRepository.existsById(created.getId()));
    }

    @Test
    @WithMockUser(authorities = { "ADMIN", "USER" })
    void getProducts_Success() throws Exception {
        productCreateService.createProduct(productCreateRequest);

        ProductPagingRequest pagingRequest = new ProductPagingRequest();

        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void unauthorizedAccess_Failure() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void insufficientPermissions_Failure() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = { "ADMIN", "USER" })
    void getProduct_NotFound() throws Exception {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        mockMvc.perform(get("/api/v1/products/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createProduct_ValidationFailure() throws Exception {
        ProductCreateRequest invalidRequest = ProductCreateRequest.builder()
                .name("")
                .amount(new BigDecimal("0.0000"))
                .unitPrice(new BigDecimal("-1.0000"))
                .build();

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}