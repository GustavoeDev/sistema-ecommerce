package br.com.gustavoedev.orders_service.modules.orders.dtos.review;

import br.com.gustavoedev.orders_service.modules.orders.dtos.product.ProductResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {

    private UUID id;
    private ProductResponseDTO product;
    private UserResponseDTO user;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}