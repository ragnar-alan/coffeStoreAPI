package com.coffee.coffeestoreapi.service.admin;

import com.coffee.coffeestoreapi.entity.Product;
import com.coffee.coffeestoreapi.mapper.ProductMapper;
import com.coffee.coffeestoreapi.model.ProductChangeRequest;
import com.coffee.coffeestoreapi.model.ProductCreateRequest;
import com.coffee.coffeestoreapi.model.ProductDto;
import com.coffee.coffeestoreapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ResponseEntity<List<ProductDto>> getProducts() {
        var productEntities = productRepository.getAllProducts();
        if (productEntities.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<ProductDto> products = productMapper.productToProductDto(productEntities);
        return ResponseEntity.ok(products);
    }

    public ResponseEntity<ProductDto> createProduct(ProductCreateRequest productChangeRequest) {
        //@TODO handle unique product names - 400
        var productEntity = new Product();
        productEntity.setProductName(productChangeRequest.productName());
        productEntity.setPriceInCents(productChangeRequest.priceInCents());
        productEntity.setType(productChangeRequest.type());
        productEntity.setIsFavorite(productChangeRequest.isFavorite());

        var savedProduct = productRepository.save(productEntity);
        return ResponseEntity.created(URI.create("/api/v1/admin/products/" + savedProduct.getId()))
                .body(productMapper.productToProductDto(savedProduct));
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
        }).orElse(ResponseEntity.notFound().build());
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