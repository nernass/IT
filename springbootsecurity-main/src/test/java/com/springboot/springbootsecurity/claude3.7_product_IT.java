package com.springboot.springbootsecurity.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springbootsecurity.common.model.CustomPage;
import com.springboot.springbootsecurity.product.controller.ProductController;
import com.springboot.springbootsecurity.product.model.Product;
import com.springboot.springbootsecurity.product.model.dto.request.ProductCreateRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductPagingRequest;
import com.springboot.springbootsecurity.product.model.dto.request.ProductUpdateRequest;
import com.springboot.springbootsecurity.product.model.entity.ProductEntity;
import com.springboot.springbootsecurity.product.repository.ProductRepository;
import com.springboot.springbootsecurity.product.service.ProductCreateService;
import com.springboot.springbootsecurity.product.service.ProductDeleteService;
import com.springboot.springbootsecurity.product.service.ProductReadService;
import com.springboot.springbootsecurity.product.service.ProductUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductIntegrationTest {

    @Nested
    @WebMvcTest(ProductController.class)
    class ProductControllerTest {

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

        private Product sampleProduct;
        private ProductCreateRequest createRequest;
        private ProductUpdateRequest updateRequest;
        private ProductPagingRequest pagingRequest;

        @BeforeEach
        public void setup() {
            sampleProduct = Product.builder()
                    .id("1234-5678-90ab-cdef")
                    .name("Test Product")
                    .amount(new BigDecimal("10.0000"))
                    .unitPrice(new BigDecimal("25.5000"))
                    .build();

            createRequest = ProductCreateRequest.builder()
                    .name("New Product")
                    .amount(new BigDecimal("5.0000"))
                    .unitPrice(new BigDecimal("15.5000"))
                    .build();

            updateRequest = ProductUpdateRequest.builder()
                    .name("Updated Product")
                    .amount(new BigDecimal("15.0000"))
                    .unitPrice(new BigDecimal("30.5000"))
                    .build();

            pagingRequest = new ProductPagingRequest();
            // Configure paging request as needed
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        public void createProduct_WithValidRequest_ShouldReturnSuccess() throws Exception {
            when(productCreateService.createProduct(any(ProductCreateRequest.class)))
                    .thenReturn(sampleProduct);

            mockMvc.perform(post("/api/v1/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data", is(sampleProduct.getId())));

            verify(productCreateService, times(1)).createProduct(any(ProductCreateRequest.class));
        }

        @Test
        @WithMockUser(authorities = "USER")
        public void createProduct_WithUserRole_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(post("/api/v1/products")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isForbidden());

            verify(productCreateService, never()).createProduct(any(ProductCreateRequest.class));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        public void getProductById_ShouldReturnProduct() throws Exception {
            when(productReadService.getProductById(eq(sampleProduct.getId())))
                    .thenReturn(sampleProduct);

            mockMvc.perform(get("/api/v1/products/{productId}", sampleProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(sampleProduct.getId())))
                    .andExpect(jsonPath("$.data.name", is(sampleProduct.getName())));

            verify(productReadService, times(1)).getProductById(sampleProduct.getId());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        public void getProducts_ShouldReturnPageOfProducts() throws Exception {
            List<Product> products = Arrays.asList(sampleProduct);
            CustomPage<Product> page = new CustomPage<>();
            page.setContent(products);
            page.setTotalElements(1L);
            page.setTotalPages(1);
            page.setSize(10);
            page.setNumber(0);

            when(productReadService.getProducts(any(ProductPagingRequest.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(pagingRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].id", is(sampleProduct.getId())))
                    .andExpect(jsonPath("$.data.totalElements", is(1)));

            verify(productReadService, times(1)).getProducts(any(ProductPagingRequest.class));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        public void updateProductById_ShouldReturnUpdatedProduct() throws Exception {
            Product updatedProduct = Product.builder()
                    .id(sampleProduct.getId())
                    .name(updateRequest.getName())
                    .amount(updateRequest.getAmount())
                    .unitPrice(updateRequest.getUnitPrice())
                    .build();

            when(productUpdateService.updateProductById(eq(sampleProduct.getId()), any(ProductUpdateRequest.class)))
                    .thenReturn(updatedProduct);

            mockMvc.perform(put("/api/v1/products/{productId}", sampleProduct.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(sampleProduct.getId())))
                    .andExpect(jsonPath("$.data.name", is(updateRequest.getName())));

            verify(productUpdateService, times(1)).updateProductById(eq(sampleProduct.getId()),
                    any(ProductUpdateRequest.class));
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        public void deleteProductById_ShouldDeleteSuccessfully() throws Exception {
            doNothing().when(productDeleteService).deleteProductById(sampleProduct.getId());

            mockMvc.perform(delete("/api/v1/products/{productId}", sampleProduct.getId())
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)));

            verify(productDeleteService, times(1)).deleteProductById(sampleProduct.getId());
        }

        @Test
        public void requestWithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/products/{productId}", sampleProduct.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(authorities = "ADMIN")
        public void invalidProductId_ShouldReturnBadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/products/invalid-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @SpringBootTest
    class ProductServiceTest {

        @Autowired
        private ProductCreateService productCreateService;

        @Autowired
        private ProductReadService productReadService;

        @Autowired
        private ProductUpdateService productUpdateService;

        @Autowired
        private ProductDeleteService productDeleteService;

        @MockBean
        private ProductRepository productRepository;

        private ProductEntity productEntity;
        private Product product;
        private ProductCreateRequest createRequest;
        private ProductUpdateRequest updateRequest;
        private ProductPagingRequest pagingRequest;

        @BeforeEach
        public void setup() {
            productEntity = ProductEntity.builder()
                    .id("1234-5678-90ab-cdef")
                    .name("Test Product")
                    .amount(new BigDecimal("10.0000"))
                    .unitPrice(new BigDecimal("25.5000"))
                    .build();

            product = Product.builder()
                    .id("1234-5678-90ab-cdef")
                    .name("Test Product")
                    .amount(new BigDecimal("10.0000"))
                    .unitPrice(new BigDecimal("25.5000"))
                    .build();

            createRequest = ProductCreateRequest.builder()
                    .name("New Product")
                    .amount(new BigDecimal("5.0000"))
                    .unitPrice(new BigDecimal("15.5000"))
                    .build();

            updateRequest = ProductUpdateRequest.builder()
                    .name("Updated Product")
                    .amount(new BigDecimal("15.0000"))
                    .unitPrice(new BigDecimal("30.5000"))
                    .build();

            pagingRequest = new ProductPagingRequest();
            // Configure paging request as needed
        }

        @Test
        public void createProduct_ShouldReturnNewProduct() {
            when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
                ProductEntity savedEntity = invocation.getArgument(0);
                savedEntity.setId("new-product-id");
                return savedEntity;
            });

            Product result = productCreateService.createProduct(createRequest);

            assertNotNull(result);
            assertEquals("new-product-id", result.getId());
            assertEquals(createRequest.getName(), result.getName());
            assertEquals(createRequest.getAmount(), result.getAmount());
            assertEquals(createRequest.getUnitPrice(), result.getUnitPrice());

            verify(productRepository, times(1)).save(any(ProductEntity.class));
        }

        @Test
        public void getProductById_ShouldReturnProduct() {
            when(productRepository.findById(productEntity.getId())).thenReturn(Optional.of(productEntity));

            Product result = productReadService.getProductById(productEntity.getId());

            assertNotNull(result);
            assertEquals(productEntity.getId(), result.getId());
            assertEquals(productEntity.getName(), result.getName());
            assertEquals(productEntity.getAmount(), result.getAmount());
            assertEquals(productEntity.getUnitPrice(), result.getUnitPrice());

            verify(productRepository, times(1)).findById(productEntity.getId());
        }

        @Test
        public void getProductById_WithInvalidId_ShouldThrowException() {
            when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class, () -> {
                productReadService.getProductById("invalid-id");
            });

            verify(productRepository, times(1)).findById("invalid-id");
        }

        @Test
        public void getProducts_ShouldReturnPageOfProducts() {
            Page<ProductEntity> productEntityPage = new PageImpl<>(
                    List.of(productEntity),
                    PageRequest.of(0, 10),
                    1);

            when(productRepository.findAll(any(Pageable.class))).thenReturn(productEntityPage);

            CustomPage<Product> result = productReadService.getProducts(pagingRequest);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(1, result.getContent().size());
            assertEquals(productEntity.getId(), result.getContent().get(0).getId());

            verify(productRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        public void updateProductById_ShouldReturnUpdatedProduct() {
            when(productRepository.findById(productEntity.getId())).thenReturn(Optional.of(productEntity));
            when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Product result = productUpdateService.updateProductById(productEntity.getId(), updateRequest);

            assertNotNull(result);
            assertEquals(productEntity.getId(), result.getId());
            assertEquals(updateRequest.getName(), result.getName());
            assertEquals(updateRequest.getAmount(), result.getAmount());
            assertEquals(updateRequest.getUnitPrice(), result.getUnitPrice());

            verify(productRepository, times(1)).findById(productEntity.getId());
            verify(productRepository, times(1)).save(any(ProductEntity.class));
        }

        @Test
        public void deleteProductById_ShouldDeleteProduct() {
            when(productRepository.findById(productEntity.getId())).thenReturn(Optional.of(productEntity));
            doNothing().when(productRepository).delete(any(ProductEntity.class));

            productDeleteService.deleteProductById(productEntity.getId());

            verify(productRepository, times(1)).findById(productEntity.getId());
            verify(productRepository, times(1)).delete(any(ProductEntity.class));
        }

        @Test
        public void deleteProductById_WithInvalidId_ShouldThrowException() {
            when(productRepository.findById("invalid-id")).thenReturn(Optional.empty());

            assertThrows(NoSuchElementException.class, () -> {
                productDeleteService.deleteProductById("invalid-id");
            });

            verify(productRepository, times(1)).findById("invalid-id");
            verify(productRepository, never()).delete(any(ProductEntity.class));
        }
    }
}