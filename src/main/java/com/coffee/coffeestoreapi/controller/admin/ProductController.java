package com.coffee.coffeestoreapi.controller.admin;


import com.coffee.coffeestoreapi.model.ProductChangeRequest;
import com.coffee.coffeestoreapi.model.ProductCreateRequest;
import com.coffee.coffeestoreapi.model.ProductDto;
import com.coffee.coffeestoreapi.service.admin.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@RestController
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProducts(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ProductDto>> getProducts() {
        return productService.getProducts();
    }

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductCreateRequest productChangeRequest) {
        return productService.createProduct(productChangeRequest);
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductChangeRequest productChangeRequest
    ) {
        return productService.updateProduct(productId, productChangeRequest);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }
}
