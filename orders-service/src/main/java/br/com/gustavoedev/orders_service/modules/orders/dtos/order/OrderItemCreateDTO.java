package br.com.gustavoedev.orders_service.modules.orders.dtos.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCreateDTO {

    @NotNull(message = "O ID do produto é obrigatório!")
    private UUID productId;

    @NotNull(message = "Quantidade é obrigatória!")
    @Min(value = 1, message = "A quantidade deve ser de pelo menos 1.")
    private Integer quantity;

}