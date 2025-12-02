package br.com.gustavoedev.orders_service.modules.orders.dtos.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateDTO {

    @NotEmpty(message = "O pedido deve conter pelo menos um item.")
    @Valid
    private List<OrderItemCreateDTO> items;

    @NotNull(message = "Endereço de entrega é obrigatório!")
    private UUID addressId;

    private String couponCode;

}