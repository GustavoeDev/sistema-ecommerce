package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.AddressNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.UserNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.AddressMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.AddressEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.AddressRepository;
import br.com.gustavoedev.orders_service.modules.orders.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Transactional
    public AddressResponseDTO createAddress(UUID userId, AddressCreateDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com id: " + userId + " não encontrado!"));

        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            addressRepository.findByUserId(userId).forEach(addr -> {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            });
        }

        AddressEntity entity = AddressEntity.builder()
                .user(user)
                .postalCode(dto.getPostalCode())
                .street(dto.getStreet())
                .number(dto.getNumber())
                .complement(dto.getComplement())
                .neighborhood(dto.getNeighborhood())
                .city(dto.getCity())
                .state(dto.getState())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();

        AddressEntity saved = addressRepository.save(entity);
        return addressMapper.toResponseDTO(saved);
    }

    public AddressResponseDTO getAddressById(UUID id) {
        AddressEntity entity = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Endereço com id: " + id + " não encontrado!"));

        return addressMapper.toResponseDTO(entity);
    }

    public List<AddressResponseDTO> getAddressesByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuário com id: " + userId + " não encontrado!");
        }

        return addressRepository.findByUserId(userId).stream()
                .map(addressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressResponseDTO updateAddress(UUID id, AddressUpdateDTO dto) {
        AddressEntity entity = addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Endereço com id: " + id + " não encontrado!"));

        if (dto.getPostalCode() != null) {
            entity.setPostalCode(dto.getPostalCode());
        }

        if (dto.getStreet() != null) {
            entity.setStreet(dto.getStreet());
        }

        if (dto.getNumber() != null) {
            entity.setNumber(dto.getNumber());
        }

        if (dto.getComplement() != null) {
            entity.setComplement(dto.getComplement());
        }

        if (dto.getNeighborhood() != null) {
            entity.setNeighborhood(dto.getNeighborhood());
        }

        if (dto.getCity() != null) {
            entity.setCity(dto.getCity());
        }

        if (dto.getState() != null) {
            entity.setState(dto.getState());
        }

        if (dto.getIsDefault() != null && dto.getIsDefault()) {
            addressRepository.findByUserId(entity.getUser().getId()).forEach(addr -> {
                if (!addr.getId().equals(id)) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            });
            entity.setIsDefault(true);
        }

        AddressEntity updated = addressRepository.save(entity);
        return addressMapper.toResponseDTO(updated);
    }

    public void deleteAddress(UUID id) {
        if (!addressRepository.existsById(id)) {
            throw new AddressNotFoundException("Endereço com id: " + id + " não encontrado!");
        }
        addressRepository.deleteById(id);
    }
}