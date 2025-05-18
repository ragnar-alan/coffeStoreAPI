package com.coffee.coffeestoreapi.mapper;

import com.coffee.coffeestoreapi.entity.Product;
import com.coffee.coffeestoreapi.model.ProductDto;
import com.coffee.coffeestoreapi.model.ProductType;
import com.coffee.coffeestoreapi.service.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest extends BaseTest {

    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    @Test
    @DisplayName("productToProductDto should correctly map Product to ProductDto")
    void productToProductDto_ShouldCorrectlyMapProductToProductDto() {
        // Given
        Product product = createTestProduct(1L, "Espresso", 250, ProductType.DRINK, true);

        // When
        ProductDto productDto = productMapper.productToProductDto(product);

        // Then
        assertNotNull(productDto);
        assertEquals(product.getId(), productDto.id());
        assertEquals(product.getProductName(), productDto.productName());
        assertEquals(product.getPriceInCents(), productDto.priceInCents());
        assertEquals(product.getType(), productDto.type());
        assertEquals(product.getIsFavorite(), productDto.isFavorite());
    }

    @Test
    @DisplayName("productToProductDto should handle null Product")
    void productToProductDto_ShouldHandleNullProduct() {
        // When
        ProductDto productDto = productMapper.productToProductDto((Product) null);

        // Then
        assertNull(productDto);
    }

    @Test
    @DisplayName("productToProductDto should handle Product with null isFavorite field")
    void productToProductDto_ShouldHandleProductWithNullIsFavoriteField() {
        // Given
        Product product = createTestProduct(1L, "Espresso", 250, ProductType.DRINK, null);

        // When
        ProductDto productDto = productMapper.productToProductDto(product);

        // Then
        assertNotNull(productDto);
        assertEquals(product.getId(), productDto.id());
        assertEquals(product.getProductName(), productDto.productName());
        assertEquals(product.getPriceInCents(), productDto.priceInCents());
        assertEquals(product.getType(), productDto.type());
        assertFalse(productDto.isFavorite()); // Should default to false when null
    }

    @Test
    @DisplayName("productToProductDto should correctly map List<Product> to List<ProductDto>")
    void productToProductDto_ShouldCorrectlyMapProductListToProductDtoList() {
        // Given
        List<Product> products = Arrays.asList(
            createTestProduct(1L, "Espresso", 250, ProductType.DRINK, true),
            createTestProduct(2L, "Latte", 350, ProductType.DRINK, false),
            createTestProduct(3L, "Milk", 50, ProductType.TOPPING, null)
        );

        // When
        List<ProductDto> productDtos = productMapper.productToProductDto(products);

        // Then
        assertNotNull(productDtos);
        assertEquals(3, productDtos.size());
        
        // Verify first product
        assertEquals(1L, productDtos.get(0).id());
        assertEquals("Espresso", productDtos.get(0).productName());
        assertEquals(250, productDtos.get(0).priceInCents());
        assertEquals(ProductType.DRINK, productDtos.get(0).type());
        assertTrue(productDtos.get(0).isFavorite());
        
        // Verify second product
        assertEquals(2L, productDtos.get(1).id());
        assertEquals("Latte", productDtos.get(1).productName());
        assertEquals(350, productDtos.get(1).priceInCents());
        assertEquals(ProductType.DRINK, productDtos.get(1).type());
        assertFalse(productDtos.get(1).isFavorite());
        
        // Verify third product (with null isFavorite)
        assertEquals(3L, productDtos.get(2).id());
        assertEquals("Milk", productDtos.get(2).productName());
        assertEquals(50, productDtos.get(2).priceInCents());
        assertEquals(ProductType.TOPPING, productDtos.get(2).type());
        assertFalse(productDtos.get(2).isFavorite()); // Should default to false when null
    }

    @Test
    @DisplayName("productToProductDto should handle empty List<Product>")
    void productToProductDto_ShouldHandleEmptyProductList() {
        // Given
        List<Product> products = Collections.emptyList();

        // When
        List<ProductDto> productDtos = productMapper.productToProductDto(products);

        // Then
        assertNotNull(productDtos);
        assertTrue(productDtos.isEmpty());
    }

    @Test
    @DisplayName("productToProductDto should handle null List<Product>")
    void productToProductDto_ShouldHandleNullProductList() {
        // When
        List<ProductDto> productDtos = productMapper.productToProductDto((List<Product>) null);

        // Then
        assertNull(productDtos);
    }

    private Product createTestProduct(Long id, String name, Integer priceInCents, ProductType type, Boolean isFavorite) {
        Product product = new Product();
        product.setId(id);
        product.setProductName(name);
        product.setPriceInCents(priceInCents);
        product.setType(type);
        product.setIsFavorite(isFavorite);
        return product;
    }
}