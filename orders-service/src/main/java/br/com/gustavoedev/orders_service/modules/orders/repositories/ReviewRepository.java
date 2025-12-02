package br.com.gustavoedev.orders_service.modules.orders.repositories;

import br.com.gustavoedev.orders_service.modules.orders.models.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {
    List<ReviewEntity> findByProductId(UUID productId);
    List<ReviewEntity> findByUserId(UUID userId);
}