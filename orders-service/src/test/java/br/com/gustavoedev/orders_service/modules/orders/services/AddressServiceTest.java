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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressService addressService;

    private UserEntity user;
    private AddressEntity address;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john@test.com")
                .build();

        address = AddressEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .postalCode("01001-000")
                .street("Rua Teste")
                .number("123")
                .city("São Paulo")
                .state("SP")
                .isDefault(true)
                .build();
    }

    @Test
    void createAddress_success() {
        UUID userId = user.getId();
        AddressCreateDTO dto = new AddressCreateDTO();
        dto.setPostalCode("01001-000");
        dto.setStreet("Rua Teste");
        dto.setNumber("123");
        dto.setCity("São Paulo");
        dto.setState("SP");
        dto.setIsDefault(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(userId)).thenReturn(List.of());
        when(addressRepository.save(any())).thenReturn(address);
        when(addressMapper.toResponseDTO(address)).thenReturn(
                AddressResponseDTO.builder().id(address.getId()).build()
        );

        AddressResponseDTO result = addressService.createAddress(userId, dto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(addressRepository).save(any());
    }

    @Test
    void createAddress_userNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.createAddress(userId, new AddressCreateDTO()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(addressRepository, never()).save(any());
    }

    @Test
    void createAddress_asDefault_removesOtherDefaults() {
        UUID userId = user.getId();
        AddressEntity existingDefault = AddressEntity.builder()
                .id(UUID.randomUUID())
                .isDefault(true)
                .build();

        AddressCreateDTO dto = new AddressCreateDTO();
        dto.setPostalCode("02002-000");
        dto.setStreet("Outra Rua");
        dto.setNumber("456");
        dto.setCity("São Paulo");
        dto.setState("SP");
        dto.setIsDefault(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(userId)).thenReturn(List.of(existingDefault));
        when(addressRepository.save(any())).thenReturn(address);
        when(addressMapper.toResponseDTO(address)).thenReturn(
                AddressResponseDTO.builder().id(address.getId()).build()
        );

        addressService.createAddress(userId, dto);

        verify(addressRepository).save(existingDefault);
        assertThat(existingDefault.getIsDefault()).isFalse();
    }

    @Test
    void getAddressById_success() {
        UUID id = address.getId();
        when(addressRepository.findById(id)).thenReturn(Optional.of(address));
        when(addressMapper.toResponseDTO(address)).thenReturn(
                AddressResponseDTO.builder().id(id).build()
        );

        AddressResponseDTO result = addressService.getAddressById(id);

        assertThat(result).isNotNull();
        verify(addressRepository).findById(id);
    }

    @Test
    void getAddressById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(id))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository).findById(id);
    }

    @Test
    void getAddressesByUserId_success() {
        UUID userId = user.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(addressRepository.findByUserId(userId)).thenReturn(List.of(address));
        when(addressMapper.toResponseDTO(address)).thenReturn(
                AddressResponseDTO.builder().id(address.getId()).build()
        );

        List<AddressResponseDTO> result = addressService.getAddressesByUserId(userId);

        assertThat(result).hasSize(1);
        verify(userRepository).existsById(userId);
        verify(addressRepository).findByUserId(userId);
    }

    @Test
    void getAddressesByUserId_userNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> addressService.getAddressesByUserId(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
    }

    @Test
    void updateAddress_success() {
        UUID id = address.getId();
        AddressUpdateDTO dto = new AddressUpdateDTO();
        dto.setStreet("Rua Atualizada");
        dto.setNumber("456");

        when(addressRepository.findById(id)).thenReturn(Optional.of(address));
        when(addressRepository.save(address)).thenReturn(address);
        when(addressMapper.toResponseDTO(address)).thenReturn(
                AddressResponseDTO.builder().id(id).build()
        );

        AddressResponseDTO result = addressService.updateAddress(id, dto);

        assertThat(result).isNotNull();
        verify(addressRepository).findById(id);
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.updateAddress(id, new AddressUpdateDTO()))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository).findById(id);
    }

    @Test
    void deleteAddress_success() {
        UUID id = address.getId();
        when(addressRepository.existsById(id)).thenReturn(true);

        addressService.deleteAddress(id);

        verify(addressRepository).existsById(id);
        verify(addressRepository).deleteById(id);
    }

    @Test
    void deleteAddress_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(addressRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> addressService.deleteAddress(id))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository).existsById(id);
        verify(addressRepository, never()).deleteById(any());
    }
}