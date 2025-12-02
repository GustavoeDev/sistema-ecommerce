package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderItemResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderItemEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final CouponMapper couponMapper;

    public OrderResponseDTO toResponseDTO(OrderEntity entity) {
        List<OrderItemResponseDTO> items = entity.getItems().stream()
                .map(this::toOrderItemResponseDTO)
                .collect(Collectors.toList());

        return OrderResponseDTO.builder()
                .id(entity.getId())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .client(userMapper.toResponseDTO(entity.getClient()))
                .address(addressMapper.toResponseDTO(entity.getAddress()))
                .coupon(entity.getCoupon() != null ? couponMapper.toResponseDTO(entity.getCoupon()) : null)
                .items(items)
                .totalAmount(entity.getTotalAmount())
                .shippingCost(entity.getShippingCost())
                .discountAmount(entity.getDiscountAmount())
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