package com.coffee.coffeestoreapi.mapper;

import com.coffee.coffeestoreapi.entity.Product;
import com.coffee.coffeestoreapi.model.ProductDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    @Mapping(target = "isFavorite", expression = "java(product.getIsFavorite() != null ? product.getIsFavorite() : false)")
    ProductDto productToProductDto(Product product);

    List<ProductDto> productToProductDto(List<Product> products);
}
