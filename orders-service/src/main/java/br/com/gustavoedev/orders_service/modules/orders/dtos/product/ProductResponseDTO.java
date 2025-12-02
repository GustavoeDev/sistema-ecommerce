package br.com.gustavoedev.orders_service.modules.orders.dtos.product;

import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryResponseDTO;
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
public class ProductResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private BigDecimal weight;
    private BigDecimal averageRating;
    private Boolean active;
    private CategoryResponseDTO category;

}