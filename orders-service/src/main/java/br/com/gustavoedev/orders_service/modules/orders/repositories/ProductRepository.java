package br.com.gustavoedev.orders_service.modules.orders.repositories;

import br.com.gustavoedev.orders_service.modules.orders.models.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findByCategoryId(UUID categoryId);
    List<ProductEntity> findByActiveTrue();
    ProductEntity findByName(String name);
}