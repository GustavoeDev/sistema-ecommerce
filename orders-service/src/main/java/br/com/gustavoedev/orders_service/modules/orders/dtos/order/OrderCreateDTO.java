package br.com.gustavoedev.orders_service.modules.orders.dtos.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    @NotEmpty(message = "O pedido deve conter pelo menos um item.")
    @Valid
    private List<OrderItemCreateDTO> items;

}