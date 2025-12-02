package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.ReviewEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewMapper {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;

    public ReviewResponseDTO toResponseDTO(ReviewEntity entity) {
        return ReviewResponseDTO.builder()
                .id(entity.getId())
                .product(productMapper.toResponseDTO(entity.getProduct()))
                .user(userMapper.toResponseDTO(entity.getUser()))
                .rating(entity.getRating())
                .comment(entity.getComment())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}