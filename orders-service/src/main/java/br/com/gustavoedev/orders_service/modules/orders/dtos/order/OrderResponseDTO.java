package br.com.gustavoedev.orders_service.modules.orders.dtos.order;

import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private UUID id;
    private LocalDateTime createdAt;
    private OrderStatus status;
    private UserResponseDTO client;
    private List<OrderItemResponseDTO> items;
    private BigDecimal totalValue;

}