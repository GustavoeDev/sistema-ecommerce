package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.UserAlreadyExistsException;
import br.com.gustavoedev.orders_service.exceptions.UserNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.UserMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private UserEntity existing;

    @BeforeEach
    void setUp() {
        existing = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("John")
                .email("john@example.com")
                .password("pass")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createUser_success() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setName("Mary");
        dto.setEmail("mary@example.com");
        dto.setPassword("pwd");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        UserEntity saved = UserEntity.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .email(dto.getEmail())
                .active(true)
                .build();

        when(userRepository.save(any())).thenReturn(saved);
        when(userMapper.toResponseDTO(saved)).thenReturn(UserResponseDTO.builder()
                .id(saved.getId()).name(saved.getName()).email(saved.getEmail()).build());

        var result = userService.createUser(dto);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(dto.getEmail());

        verify(userRepository).findByEmail(dto.getEmail());
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toResponseDTO(saved);
    }

    @Test
    void createUser_whenEmailExists_shouldThrow() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail(existing.getEmail());

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(dto.getEmail());

        verify(userRepository).findByEmail(dto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUsers_byId_success() {
        UUID id = existing.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userMapper.toResponseDTO(existing)).thenReturn(UserResponseDTO.builder().id(id).name(existing.getName()).build());

        var list = userService.getUsers(id, null);

        assertThat(list).hasSize(1);
        verify(userRepository).findById(id);
    }

    @Test
    void getUsers_byId_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUsers(id, null))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(id);
    }

    @Test
    void getUsers_byEmail_success() {
        when(userRepository.findByEmail(existing.getEmail())).thenReturn(Optional.of(existing));
        when(userMapper.toResponseDTO(existing)).thenReturn(UserResponseDTO.builder().id(existing.getId()).email(existing.getEmail()).build());

        var list = userService.getUsers(null, existing.getEmail());

        assertThat(list).hasSize(1);
        verify(userRepository).findByEmail(existing.getEmail());
    }

    @Test
    void getUsers_all_success() {
        UserEntity u2 = UserEntity.builder().id(UUID.randomUUID()).email("b@b.com").build();
        when(userRepository.findAll()).thenReturn(List.of(existing, u2));
        when(userMapper.toResponseDTO(existing)).thenReturn(UserResponseDTO.builder().id(existing.getId()).email(existing.getEmail()).build());
        when(userMapper.toResponseDTO(u2)).thenReturn(UserResponseDTO.builder().id(u2.getId()).email(u2.getEmail()).build());

        var list = userService.getUsers(null, null);

        assertThat(list).hasSize(2);
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_success_changeFields() {
        UUID id = existing.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("NewName");
        dto.setActive(false);
        dto.setPassword("newpass");

        when(userRepository.save(existing)).thenReturn(existing);
        when(userMapper.toResponseDTO(existing)).thenReturn(UserResponseDTO.builder().id(id).name("NewName").build());

        var result = userService.updateUser(id, dto);

        assertThat(result.getName()).isEqualTo("NewName");
        verify(userRepository).findById(id);
        verify(userRepository).save(existing);
        verify(userMapper).toResponseDTO(existing);
    }

    @Test
    void updateUser_changeEmail_conflict_shouldThrow() {
        UUID id = existing.getId();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setEmail("other@example.com");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new UserEntity()));

        assertThatThrownBy(() -> userService.updateUser(id, dto))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository).findById(id);
        verify(userRepository).findByEmail(dto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(id, new UserUpdateDTO()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(id);
    }

    @Test
    void deleteUser_success() {
        UUID id = existing.getId();
        when(userRepository.existsById(id)).thenReturn(true);

        userService.deleteUser(id);

        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void deleteUser_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(id))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(id);
    }
}
