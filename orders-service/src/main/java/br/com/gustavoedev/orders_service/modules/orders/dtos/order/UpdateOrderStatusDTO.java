package br.com.gustavoedev.orders_service.modules.orders.dtos.order;

import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusDTO {

    @NotNull(message = "O status do pedido é obrigatório!")
    private OrderStatus status;

}
