package br.com.gustavoedev.orders_service.modules.orders.dtos.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ProductCreateDTO {

    @NotBlank(message = "Nome é obrigatório!")
    private String name;

    @NotNull(message = "Preço é obrigatório!")
    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero!")
    private BigDecimal price;

    @NotNull(message = "A quantidade em estoque é necessária!")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Categoria é obrigatória!")
    private UUID categoryId;

}