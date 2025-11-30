package br.com.gustavoedev.orders_service.modules.orders.repositories;

import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
}