package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponseDTO toResponseDTO(CategoryEntity entity) {
        return CategoryResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}