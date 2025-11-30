package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.UserAlreadyExistsException;
import br.com.gustavoedev.orders_service.exceptions.UserNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.user.UserUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.UserMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDTO createUser(UserCreateDTO dto) {
        userRepository.findByEmail(dto.getEmail())
            .ifPresent(user -> {
                throw new UserAlreadyExistsException("Usuário com e-mail '" + dto.getEmail() + "' já existe!");
            });

        UserEntity entity = new UserEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setActive(true);
        entity.setPassword(dto.getPassword());

        UserEntity saved = userRepository.save(entity);
        return userMapper.toResponseDTO(saved);
    }

    public List<UserResponseDTO> getUsers(UUID id, String email) {
        if (id != null) {
            UserEntity entity = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException("Usuário com id: " + id + " não encontrado!"));
            return List.of(userMapper.toResponseDTO(entity));
        }

        if (email != null) {
            UserEntity entity = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("Usuário com e-mail: " + email + " não encontrado!"));
            return List.of(userMapper.toResponseDTO(entity));
        }

        return userRepository.findAll().stream()
                .map(userMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUser(UUID id, UserUpdateDTO dto) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário com id: " + id + " não encontrado!"));

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getEmail() != null && !dto.getEmail().equals(entity.getEmail())) {
            userRepository.findByEmail(dto.getEmail())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("Usuário com e-mail: '" + dto.getEmail() + "' já existe!");
                });
            entity.setEmail(dto.getEmail());
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        if (dto.getPassword() != null) {
            entity.setPassword(dto.getPassword());
        }

        UserEntity updated = userRepository.save(entity);
        return userMapper.toResponseDTO(updated);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("Usuário com id: " + id + " não encontrado!");
        }
        userRepository.deleteById(id);
    }
}