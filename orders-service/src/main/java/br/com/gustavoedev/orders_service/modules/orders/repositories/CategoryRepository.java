package br.com.gustavoedev.orders_service.modules.orders.repositories;

import br.com.gustavoedev.orders_service.modules.orders.models.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByName(String name);
}
