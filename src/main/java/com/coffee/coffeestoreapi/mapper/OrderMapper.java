package com.coffee.coffeestoreapi.mapper;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "createdAt", source = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    OrderDto orderToOrderDto(Order order);


}
