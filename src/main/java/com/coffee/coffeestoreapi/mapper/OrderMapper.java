package com.coffee.coffeestoreapi.mapper;

import com.coffee.coffeestoreapi.entity.Order;
import com.coffee.coffeestoreapi.model.OrderDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderDto orderToOrderDto(Order order);

    List<OrderDto> orderListToOrderDtoList(List<Order> orders);


}
