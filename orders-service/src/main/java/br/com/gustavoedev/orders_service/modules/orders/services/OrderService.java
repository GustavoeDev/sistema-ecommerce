package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.InsufficientStockException;
import br.com.gustavoedev.orders_service.exceptions.OrderNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.ProductInactiveException;
import br.com.gustavoedev.orders_service.exceptions.ProductNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.UserNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import br.com.gustavoedev.orders_service.modules.orders.mappers.OrderMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.OrderItemEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.ProductEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.OrderRepository;
import br.com.gustavoedev.orders_service.modules.orders.repositories.ProductRepository;
import br.com.gustavoedev.orders_service.modules.orders.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateDTO dto, UUID clientId) {
        UserEntity client = userRepository.findById(clientId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com id: " + clientId + "não encontrado!"));

        OrderEntity order = new OrderEntity();
        order.setClient(client);
        order.setStatus(OrderStatus.WAITING_PAYMENT);

        List<OrderItemEntity> items = dto.getItems().stream().map(itemDTO -> {
            ProductEntity product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Produto com o id: " + itemDTO.getProductId() + "não encontrado!"));

            if (!product.getActive()) {
                throw new ProductInactiveException("Produto '" + product.getName() + "' não está ativo.");
            }

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new InsufficientStockException(
                    "Estoque insuficiente para o produto '" + product.getName() + "'. Disponível: " + product.getStockQuantity()
                );
            }

            product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
            productRepository.save(product);

            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setOrder(order);

            return orderItem;
        }).collect(Collectors.toList());

        order.setItems(items);

        OrderEntity savedOrder = orderRepository.save(order);

        return orderMapper.toResponseDTO(savedOrder);
    }

    public List<OrderResponseDTO> getOrders(UUID id, UUID clientId, OrderStatus status) {
        if (id != null) {
            return List.of(
                orderRepository.findById(id)
                    .map(orderMapper::toResponseDTO)
                    .orElseThrow(() ->
                            new OrderNotFoundException("Pedido com id: " + id + " não encontrado!")
                    )
            );
        }

        if (clientId != null) {
            if (!userRepository.existsById(clientId)) {
                throw new UserNotFoundException("Usuário com id: " + clientId + " não encontrado!");
            }

            return orderRepository.findByClientId(clientId).stream()
                    .map(orderMapper::toResponseDTO)
                    .collect(Collectors.toList());
        }

        if (status != null) {
            return orderRepository.findByStatus(status).stream()
                    .map(orderMapper::toResponseDTO)
                    .collect(Collectors.toList());
        }

        return orderRepository.findAll().stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public OrderResponseDTO updateOrderStatus(UUID id, OrderStatus newStatus) {
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido com o id: " + id + "não encontrado!"));

        entity.setStatus(newStatus);
        OrderEntity updated = orderRepository.save(entity);

        return orderMapper.toResponseDTO(updated);
    }

    public void cancelOrder(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido com o id: " + id + "não encontrado!"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Não é possível cancelar o pedido com status: " + order.getStatus());
        }

        order.getItems().forEach(item -> {
            ProductEntity product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}