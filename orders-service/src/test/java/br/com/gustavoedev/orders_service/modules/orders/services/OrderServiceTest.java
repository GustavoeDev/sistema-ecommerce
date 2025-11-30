package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.InsufficientStockException;
import br.com.gustavoedev.orders_service.exceptions.OrderNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.ProductInactiveException;
import br.com.gustavoedev.orders_service.exceptions.ProductNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.UserNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderItemCreateDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UserEntity client;

    @BeforeEach
    void setUp() {
        client = UserEntity.builder().id(UUID.randomUUID()).name("Client").build();
    }

    @Test
    void createOrder_success_shouldSaveOrderAndReduceStock() {
        UUID clientId = client.getId();
        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));

        UUID productId = UUID.randomUUID();
        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(2);
        OrderCreateDTO orderCreate = new OrderCreateDTO();
        orderCreate.setItems(List.of(itemDTO));

        ProductEntity product = ProductEntity.builder()
                .id(productId)
                .name("Prod")
                .price(BigDecimal.valueOf(10))
                .stockQuantity(5)
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

        OrderEntity savedOrder = OrderEntity.builder().id(UUID.randomUUID()).client(client).build();
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        OrderResponseDTO responseDTO = OrderResponseDTO.builder().id(savedOrder.getId()).build();
        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.createOrder(orderCreate, clientId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedOrder.getId());

        verify(userRepository).findById(clientId);
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toResponseDTO(savedOrder);
    }

    @Test
    void createOrder_userNotFound_shouldThrow() {
        UUID clientId = UUID.randomUUID();
        when(userRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(new OrderCreateDTO(), clientId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(clientId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void createOrder_productNotFound_shouldThrow() {
        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));

        UUID productId = UUID.randomUUID();
        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(1);
        OrderCreateDTO dto = new OrderCreateDTO(); dto.setItems(List.of(itemDTO));

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(dto, client.getId()))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(productId);
    }

    @Test
    void createOrder_productInactive_shouldThrow() {
        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));

        UUID productId = UUID.randomUUID();
        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(1);
        OrderCreateDTO dto = new OrderCreateDTO(); dto.setItems(List.of(itemDTO));

        ProductEntity product = ProductEntity.builder().id(productId).active(false).name("X").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(dto, client.getId()))
                .isInstanceOf(ProductInactiveException.class);

        verify(productRepository).findById(productId);
    }

    @Test
    void createOrder_insufficientStock_shouldThrow() {
        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));

        UUID productId = UUID.randomUUID();
        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(10);
        OrderCreateDTO dto = new OrderCreateDTO(); dto.setItems(List.of(itemDTO));

        ProductEntity product = ProductEntity.builder().id(productId).active(true).stockQuantity(3).name("X").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(dto, client.getId()))
                .isInstanceOf(InsufficientStockException.class);

        verify(productRepository).findById(productId);
    }

    @Test
    void getOrders_byId_success() {
        UUID id = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(id).build();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderMapper.toResponseDTO(order)).thenReturn(OrderResponseDTO.builder().id(id).build());

        var list = orderService.getOrders(id, null, null);

        assertThat(list).hasSize(1);
        verify(orderRepository).findById(id);
    }

    @Test
    void getOrders_byId_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrders(id, null, null))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(id);
    }

    @Test
    void getOrders_byClientId_success() {
        UUID clientId = client.getId();
        OrderEntity order = OrderEntity.builder().id(UUID.randomUUID()).client(client).build();

        when(userRepository.existsById(clientId)).thenReturn(true);
        when(orderRepository.findByClientId(clientId)).thenReturn(List.of(order));
        when(orderMapper.toResponseDTO(order)).thenReturn(OrderResponseDTO.builder().id(order.getId()).build());

        var list = orderService.getOrders(null, clientId, null);

        assertThat(list).hasSize(1);
        verify(userRepository).existsById(clientId);
        verify(orderRepository).findByClientId(clientId);
    }

    @Test
    void getOrders_byClientId_userNotFound_shouldThrow() {
        UUID clientId = client.getId();
        when(userRepository.existsById(clientId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.getOrders(null, clientId, null))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(clientId);
    }

    @Test
    void getOrders_byStatus_success() {
        OrderEntity o1 = OrderEntity.builder().id(UUID.randomUUID()).status(OrderStatus.WAITING_PAYMENT).build();
        when(orderRepository.findByStatus(OrderStatus.WAITING_PAYMENT)).thenReturn(List.of(o1));
        when(orderMapper.toResponseDTO(o1)).thenReturn(OrderResponseDTO.builder().id(o1.getId()).status(o1.getStatus()).build());

        var list = orderService.getOrders(null, null, OrderStatus.WAITING_PAYMENT);

        assertThat(list).hasSize(1);
        verify(orderRepository).findByStatus(OrderStatus.WAITING_PAYMENT);
    }

    @Test
    void getOrders_all_success() {
        OrderEntity o1 = OrderEntity.builder().id(UUID.randomUUID()).build();
        when(orderRepository.findAll()).thenReturn(List.of(o1));
        when(orderMapper.toResponseDTO(o1)).thenReturn(OrderResponseDTO.builder().id(o1.getId()).build());

        var list = orderService.getOrders(null, null, null);

        assertThat(list).hasSize(1);
        verify(orderRepository).findAll();
    }

    @Test
    void updateOrderStatus_success() {
        UUID id = UUID.randomUUID();
        OrderEntity entity = OrderEntity.builder().id(id).status(OrderStatus.WAITING_PAYMENT).build();
        when(orderRepository.findById(id)).thenReturn(Optional.of(entity));
        when(orderRepository.save(entity)).thenReturn(entity);
        when(orderMapper.toResponseDTO(entity)).thenReturn(OrderResponseDTO.builder().id(id).status(OrderStatus.PAID).build());

        var resp = orderService.updateOrderStatus(id, OrderStatus.PAID);

        assertThat(resp).isNotNull();
        verify(orderRepository).findById(id);
        verify(orderRepository).save(entity);
    }

    @Test
    void updateOrderStatus_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(id, OrderStatus.CANCELLED))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(id);
    }

    @Test
    void cancelOrder_success_restoresStockAndCancels() {
        UUID id = UUID.randomUUID();
        ProductEntity p = ProductEntity.builder().id(UUID.randomUUID()).stockQuantity(1).build();
        OrderItemEntity item = OrderItemEntity.builder().product(p).quantity(2).build();
        OrderEntity order = OrderEntity.builder().id(id).status(OrderStatus.WAITING_PAYMENT).items(List.of(item)).build();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancelOrder(id);

        verify(orderRepository).findById(id);
        verify(productRepository).save(p);
        verify(orderRepository).save(order);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(id))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderRepository).findById(id);
    }

    @Test
    void cancelOrder_illegalState_shouldThrow() {
        UUID id = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(id).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(id))
                .isInstanceOf(IllegalStateException.class);

        verify(orderRepository).findById(id);
    }
}
