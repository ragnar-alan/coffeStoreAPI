package com.coffee.coffeestoreapi.service.admin;

import com.coffee.coffeestoreapi.entity.Product;
import com.coffee.coffeestoreapi.exception.ProductAlreadyExistsException;
import com.coffee.coffeestoreapi.mapper.ProductMapper;
import com.coffee.coffeestoreapi.model.ProductChangeRequest;
import com.coffee.coffeestoreapi.model.ProductCreateRequest;
import com.coffee.coffeestoreapi.model.ProductDto;
import com.coffee.coffeestoreapi.model.ProductType;
import com.coffee.coffeestoreapi.repository.ProductRepository;
import com.coffee.coffeestoreapi.service.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class ProductServiceTest extends BaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("getProduct should return product when found")
    void getProduct_ShouldReturnProduct_WhenFound() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId);
        ProductDto productDto = createTestProductDto(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.productToProductDto(product)).thenReturn(productDto);

        // When
        ResponseEntity<ProductDto> response = productService.getProduct(productId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productDto, response.getBody());
        verify(productRepository).findById(productId);
        verify(productMapper).productToProductDto(product);
    }

    @Test
    @DisplayName("getProduct should return not found when product doesn't exist")
    void getProduct_ShouldReturnNotFound_WhenProductDoesNotExist() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProductDto> response = productService.getProduct(productId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productRepository).findById(productId);
        verify(productMapper, never()).productToProductDto(any(Product.class));
    }

    @Test
    @DisplayName("getProducts should return all products")
    void getProducts_ShouldReturnAllProducts() {
        // Given
        List<Product> products = List.of(
            createTestProduct(1L),
            createTestProduct(2L)
        );

        List<ProductDto> productDtos = List.of(
            createTestProductDto(1L),
            createTestProductDto(2L)
        );

        when(productRepository.getAllProducts()).thenReturn(products);
        when(productMapper.productToProductDto(products)).thenReturn(productDtos);

        // When
        ResponseEntity<List<ProductDto>> response = productService.getProducts();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productDtos, response.getBody());
        verify(productRepository).getAllProducts();
        verify(productMapper).productToProductDto(products);
    }

    @Test
    @DisplayName("getProducts should return empty list when no products exist")
    void getProducts_ShouldReturnEmptyList_WhenNoProductsExist() {
        // Given
        when(productRepository.getAllProducts()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<ProductDto>> response = productService.getProducts();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(productRepository).getAllProducts();
        verify(productMapper, never()).productToProductDto(anyList());
    }

    @Test
    @DisplayName("createProduct should create and return product")
    void createProduct_ShouldCreateAndReturnProduct() {
        // Given
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "Test Product",
            500,
            ProductType.DRINK,
            true
        );

        Product savedProduct = createTestProduct(1L);

        when(productRepository.findByProductName(createRequest.productName())).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ResponseEntity<String> response = productService.createProduct(createRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(URI.create("/api/v1/admin/products/1"), response.getHeaders().getLocation());
        verify(productRepository).findByProductName(createRequest.productName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct should throw exception when product already exists")
    void createProduct_ShouldThrowException_WhenProductAlreadyExists() {
        // Given
        ProductCreateRequest createRequest = new ProductCreateRequest(
            "Test Product",
            500,
            ProductType.DRINK,
            true
        );

        Product existingProduct = createTestProduct(1L);

        when(productRepository.findByProductName(createRequest.productName())).thenReturn(Optional.of(existingProduct));

        // When & Then
        assertThrows(ProductAlreadyExistsException.class, () -> productService.createProduct(createRequest));
        verify(productRepository).findByProductName(createRequest.productName());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("updateProduct should update and return product")
    void updateProduct_ShouldUpdateAndReturnProduct() {
        // Given
        Long productId = 1L;
        ProductChangeRequest changeRequest = new ProductChangeRequest(
            "Updated Product",
            600,
            ProductType.TOPPING,
            false
        );

        Product existingProduct = createTestProduct(productId);
        Product updatedProduct = createTestProduct(productId);
        updatedProduct.setProductName("Updated Product");
        updatedProduct.setPriceInCents(600);
        updatedProduct.setType(ProductType.TOPPING);
        updatedProduct.setIsFavorite(false);

        ProductDto updatedProductDto = createTestProductDto(productId);
        updatedProductDto = new ProductDto(
            productId,
            "Updated Product",
            600,
            ProductType.TOPPING,
            false
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.productToProductDto(updatedProduct)).thenReturn(updatedProductDto);

        // When
        ResponseEntity<ProductDto> response = productService.updateProduct(productId, changeRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProductDto, response.getBody());
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).productToProductDto(updatedProduct);
    }

    @Test
    @DisplayName("updateProduct should handle partial updates")
    void updateProduct_ShouldHandlePartialUpdates() {
        // Given
        Long productId = 1L;
        ProductChangeRequest changeRequest = new ProductChangeRequest(
            null,
            600,
            null,
            null
        );

        Product existingProduct = createTestProduct(productId);
        Product updatedProduct = createTestProduct(productId);
        updatedProduct.setPriceInCents(600);

        ProductDto updatedProductDto = createTestProductDto(productId);
        updatedProductDto = new ProductDto(
            productId,
            "Test Product",
            600,
            ProductType.DRINK,
            true
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
        when(productMapper.productToProductDto(updatedProduct)).thenReturn(updatedProductDto);

        // When
        ResponseEntity<ProductDto> response = productService.updateProduct(productId, changeRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProductDto, response.getBody());
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
        verify(productMapper).productToProductDto(updatedProduct);
    }

    @Test
    @DisplayName("updateProduct should return not found when product doesn't exist")
    void updateProduct_ShouldReturnNotFound_WhenProductDoesNotExist() {
        // Given
        Long productId = 1L;
        ProductChangeRequest changeRequest = new ProductChangeRequest(
            "Updated Product",
            600,
            ProductType.TOPPING,
            false
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<ProductDto> response = productService.updateProduct(productId, changeRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
        verify(productMapper, never()).productToProductDto(any(Product.class));
    }

    @Test
    @DisplayName("deleteProduct should delete product")
    void deleteProduct_ShouldDeleteProduct() {
        // Given
        Long productId = 1L;
        Product product = createTestProduct(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ResponseEntity<Void> response = productService.deleteProduct(productId);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    @DisplayName("deleteProduct should return not found when product doesn't exist")
    void deleteProduct_ShouldReturnNotFound_WhenProductDoesNotExist() {
        // Given
        Long productId = 1L;
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ResponseEntity<Void> response = productService.deleteProduct(productId);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any());
    }

    // Helper methods
    private Product createTestProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setProductName("Test Product");
        product.setPriceInCents(500);
        product.setType(ProductType.DRINK);
        product.setIsFavorite(true);
        return product;
    }

    private ProductDto createTestProductDto(Long id) {
        return new ProductDto(
            id,
            "Test Product",
            500,
            ProductType.DRINK,
            true
        );
    }
}
