package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.product.ProductResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.ProductEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final CategoryMapper categoryMapper;

    public ProductResponseDTO toResponseDTO(ProductEntity entity) {
        return ProductResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stockQuantity(entity.getStockQuantity())
                .weight(entity.getWeight())
                .averageRating(entity.getAverageRating())
                .active(entity.getActive())
                .category(categoryMapper.toResponseDTO(entity.getCategory()))
                .build();
    }
}