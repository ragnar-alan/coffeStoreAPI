package com.coffee.coffeestoreapi.controller.admin;


import com.coffee.coffeestoreapi.model.PopularItemsDto;
import com.coffee.coffeestoreapi.model.ProductChangeRequest;
import com.coffee.coffeestoreapi.model.ProductCreateRequest;
import com.coffee.coffeestoreapi.model.ProductDto;
import com.coffee.coffeestoreapi.service.OrderService;
import com.coffee.coffeestoreapi.service.admin.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final OrderService orderService;

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves the details of a specific product by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getProducts(@PathVariable Long productId) {
        return productService.getProduct(productId);
    }

    @Operation(
            summary = "List all products",
            description = "Retrieves a list of all products."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of products",
                    content = @Content(schema = @Schema(implementation = ProductDto.class)))
    })
    @GetMapping("/list")
    public ResponseEntity<List<ProductDto>> getProducts() {
        return productService.getProducts();
    }

    @Operation(
            summary = "Create a new product",
            description = "Creates a new product with the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductCreateRequest productChangeRequest) {
        return productService.createProduct(productChangeRequest);
    }

    @Operation(
            summary = "Update a product",
            description = "Updates an existing product with the provided changes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PatchMapping("/{productId}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductChangeRequest productChangeRequest
    ) {
        return productService.updateProduct(productId, productChangeRequest);
    }

    @Operation(
            summary = "Delete a product",
            description = "Deletes a product by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        return productService.deleteProduct(productId);
    }

    @Operation(
            summary = "Get most popular drink and topping",
            description = "Retrieves the most popular drink and topping across all orders."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Most popular items found",
                    content = @Content(schema = @Schema(implementation = PopularItemsDto.class)))
    })
    @GetMapping("/most-popular")
    public ResponseEntity<PopularItemsDto> getMostPopularItems() {
        return orderService.getMostPopularItems();
    }
}
