package br.com.gustavoedev.orders_service.modules.orders.dtos.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class ProductUpdateDTO {

    private String name;

    @DecimalMin(value = "0.01", message = "Preço deve ser maior que zero!")
    private BigDecimal price;

    @Min(value = 0, message = "Quantidade em estoque não pode ser negativa!")
    private Integer stockQuantity;

    private Boolean active;

    private UUID categoryId;

}