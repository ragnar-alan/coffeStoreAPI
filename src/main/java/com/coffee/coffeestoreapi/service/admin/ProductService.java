package com.coffee.coffeestoreapi.service.admin;

import com.coffee.coffeestoreapi.entity.Product;
import com.coffee.coffeestoreapi.exception.ProductAlreadyExistsException;
import com.coffee.coffeestoreapi.mapper.ProductMapper;
import com.coffee.coffeestoreapi.model.ProductChangeRequest;
import com.coffee.coffeestoreapi.model.ProductCreateRequest;
import com.coffee.coffeestoreapi.model.ProductDto;
import com.coffee.coffeestoreapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ResponseEntity<ProductDto> getProduct(Long productId) {
        var productEntity = productRepository.findById(productId);
        return productEntity.map(product -> ResponseEntity.ok(productMapper.productToProductDto(product)))
                .orElseGet(() -> {
                    log.error("Product not found when getting the product with the given id: {}", productId);
                    return ResponseEntity.notFound().build();
                });
    }

    public ResponseEntity<List<ProductDto>> getProducts() {
        var productEntities = productRepository.getAllProducts();
        if (productEntities.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<ProductDto> products = productMapper.productToProductDto(productEntities);
        return ResponseEntity.ok(products);
    }

    public ResponseEntity<String> createProduct(ProductCreateRequest productChangeRequest) {
        var existingProductOpt = productRepository.findByProductName(productChangeRequest.productName());
        existingProductOpt.ifPresent(product -> {
            log.error("Product with name {} already exists", productChangeRequest.productName());
            throw new ProductAlreadyExistsException("Product with name %s already exists".formatted(productChangeRequest.productName()));
        });

        var productEntity = new Product();
        productEntity.setProductName(productChangeRequest.productName());
        productEntity.setPriceInCents(productChangeRequest.priceInCents());
        productEntity.setType(productChangeRequest.type());
        productEntity.setIsFavorite(productChangeRequest.isFavorite());

        var savedProduct = productRepository.save(productEntity);
        return ResponseEntity.created(URI.create("/api/v1/admin/products/%s".formatted(savedProduct.getId()))).build();
    }

    public ResponseEntity<ProductDto> updateProduct(Long productId, ProductChangeRequest productChangeRequest) {
        var productEntity = productRepository.findById(productId);
        return productEntity.map(product -> {
            product.setProductName(productChangeRequest.productName() != null ? productChangeRequest.productName() : product.getProductName());
            product.setPriceInCents(productChangeRequest.priceInCents() != null ? productChangeRequest.priceInCents() : product.getPriceInCents());
            product.setType(productChangeRequest.type() != null ? productChangeRequest.type() : product.getType());
            product.setIsFavorite(productChangeRequest.isFavorite() != null ? productChangeRequest.isFavorite() : product.getIsFavorite());
            var savedProduct = productRepository.save(product);
            return ResponseEntity.ok(productMapper.productToProductDto(savedProduct));
        }).orElseGet(() -> {
            log.error("Product not found when updating the product with the given id: {}", productId);
            return ResponseEntity.notFound().build();
        });
    }

    public ResponseEntity<Void> deleteProduct(Long productId) {
        var productEntity = productRepository.findById(productId);
        if (productEntity.isPresent()) {
            productRepository.delete(productEntity.get());
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}