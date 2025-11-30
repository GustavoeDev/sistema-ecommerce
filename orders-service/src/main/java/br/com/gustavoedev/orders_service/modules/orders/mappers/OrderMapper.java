package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderItemResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;

    public OrderResponseDTO toResponseDTO(OrderEntity entity) {
        List<OrderItemResponseDTO> items = entity.getItems().stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList());

        BigDecimal totalValue = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .client(userMapper.toResponseDTO(entity.getClient()))
                .items(items)
                .totalValue(totalValue)
                .build();
    }

    private OrderItemResponseDTO toOrderItemResponseDTO(OrderItemEntity entity) {
        return OrderItemResponseDTO.builder()
                .id(entity.getId())
                .product(productMapper.toResponseDTO(entity.getProduct()))
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .build();
    }
}