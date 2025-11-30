package br.com.gustavoedev.orders_service.modules.orders.dtos.order;

import br.com.gustavoedev.orders_service.modules.orders.dtos.product.ProductResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {

    private UUID id;
    private ProductResponseDTO product;
    private Integer quantity;
    private BigDecimal price;

}