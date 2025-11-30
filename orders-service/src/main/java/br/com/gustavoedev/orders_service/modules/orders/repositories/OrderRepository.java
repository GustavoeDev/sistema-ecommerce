package br.com.gustavoedev.orders_service.modules.orders.repositories;

import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    List<OrderEntity> findByClientId(UUID clientId);
    List<OrderEntity> findByStatus(OrderStatus status);
}