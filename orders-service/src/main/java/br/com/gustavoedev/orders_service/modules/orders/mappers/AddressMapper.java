package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.AddressEntity;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponseDTO toResponseDTO(AddressEntity entity) {
        return AddressResponseDTO.builder()
                .id(entity.getId())
                .postalCode(entity.getPostalCode())
                .street(entity.getStreet())
                .number(entity.getNumber())
                .complement(entity.getComplement())
                .neighborhood(entity.getNeighborhood())
                .city(entity.getCity())
                .state(entity.getState())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}