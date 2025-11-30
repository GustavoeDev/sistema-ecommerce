package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponseDTO(UserEntity entity) {
        return UserResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}