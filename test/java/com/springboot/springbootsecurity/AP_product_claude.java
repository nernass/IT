package com.springboot.springbootsecurity.product.integration;

import com.springboot.springbootsecurity.common.model.CustomPage;
import com.springboot.springbootsecurity.product.model.Product;
import com.springboot.springbootsecurity.product.model.dto.request.ProductCreateRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductPagingRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductUpdateRequest;
import com.springboot.springbootsecurity.product.model.dto.response.ProductResponse;
import com.springboot.springbootsecurity.product.service.ProductCreateService;
import com.springboot.springbootsecurity.product.service.ProductDeleteService;
import com.springboot.springbootsecurity.product.service.ProductReadService;
import com.springboot.springbootsecurity.product.service.ProductUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductCreateService productCreateService;

    @MockBean
    private ProductReadService productReadService;

    @MockBean
    private ProductUpdateService productUpdateService;

    @MockBean
    private ProductDeleteService productDeleteService;

    private Product testProduct;
    private ProductCreateRequest createRequest;
    private ProductUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .name("Test Product")
                .amount(new BigDecimal("10.0000"))
                .unitPrice(new BigDecimal("99.9900"))
                .build();

        createRequest = ProductCreateRequest.builder()
                .name("Test Product")
                .amount(new BigDecimal("10.0000"))
                .unitPrice(new BigDecimal("99.9900"))
                .build();

        updateRequest = ProductUpdateRequest.builder()
                .name("Updated Product")
                .amount(new BigDecimal("20.0000"))
                .unitPrice(new BigDecimal("199.9900"))
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void createProduct_Success() throws Exception {
        when(productCreateService.createProduct(any(ProductCreateRequest.class)))
                .thenReturn(testProduct);

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(testProduct.getId()));
    }

    @Test
    @WithMockUser(authorities = { "ADMIN", "USER" })
    void getProductById_Success() throws Exception {
        when(productReadService.getProductById(testProduct.getId()))
                .thenReturn(testProduct);

        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testProduct.getId()))
                .andExpect(jsonPath("$.data.name").value(testProduct.getName()));
    }

    @Test
    @WithMockUser(authorities = { "ADMIN", "USER" })
    void getProducts_Success() throws Exception {
        CustomPage<Product> customPage = new CustomPage<>();
        customPage.setContent(Arrays.asList(testProduct));
        customPage.setTotalElements(1L);

        when(productReadService.getProducts(any(ProductPagingRequest.class)))
                .thenReturn(customPage);

        mockMvc.perform(get("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProductPagingRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(testProduct.getId()));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void updateProduct_Success() throws Exception {
        Product updatedProduct = Product.builder()
                .id(testProduct.getId())
                .name("Updated Product")
                .amount(new BigDecimal("20.0000"))
                .unitPrice(new BigDecimal("199.9900"))
                .build();

        when(productUpdateService.updateProductById(eq(testProduct.getId()), any(ProductUpdateRequest.class)))
                .thenReturn(updatedProduct);

        mockMvc.perform(put("/api/v1/products/{id}", testProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Updated Product"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void deleteProduct_Success() throws Exception {
        doNothing().when(productDeleteService).deleteProductById(testProduct.getId());

        mockMvc.perform(delete("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isOk());

        verify(productDeleteService, times(1)).deleteProductById(testProduct.getId());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void createProduct_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProduct_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId()))
                .andExpect(status().isUnauthorized());
    }
}