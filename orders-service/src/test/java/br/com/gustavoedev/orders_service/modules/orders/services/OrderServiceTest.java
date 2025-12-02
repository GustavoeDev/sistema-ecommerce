package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.*;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderItemCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import br.com.gustavoedev.orders_service.modules.orders.mappers.OrderMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.*;
import br.com.gustavoedev.orders_service.modules.orders.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private AddressRepository addressRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private UserEntity client;
    private AddressEntity address;
    private CouponEntity coupon;
    private ProductEntity product;

    @BeforeEach
    void setUp() {
        client = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("Client")
                .email("client@test.com")
                .active(true)
                .build();

        address = AddressEntity.builder()
                .id(UUID.randomUUID())
                .user(client)
                .postalCode("01001-000")
                .street("Rua Teste")
                .number("123")
                .city("São Paulo")
                .state("SP")
                .isDefault(true)
                .build();

        coupon = CouponEntity.builder()
                .id(UUID.randomUUID())
                .code("TEST10")
                .discountPercentage(BigDecimal.valueOf(10))
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(1))
                .maxTotalUses(100)
                .currentUses(0)
                .active(true)
                .minimumPurchaseAmount(BigDecimal.valueOf(50))
                .build();

        product = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product Test")
                .price(BigDecimal.valueOf(100))
                .stockQuantity(10)
                .weight(BigDecimal.valueOf(1.5))
                .active(true)
                .build();
    }

    @Test
    void createOrder_success_withCoupon_shouldSaveOrderAndReduceStock() {
        UUID clientId = client.getId();
        UUID addressId = address.getId();
        UUID productId = product.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));

        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(2);

        OrderCreateDTO orderCreate = new OrderCreateDTO();
        orderCreate.setItems(List.of(itemDTO));
        orderCreate.setAddressId(addressId);
        orderCreate.setCouponCode("TEST10");

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

        OrderEntity savedOrder = OrderEntity.builder()
                .id(UUID.randomUUID())
                .client(client)
                .address(address)
                .coupon(coupon)
                .status(OrderStatus.WAITING_PAYMENT)
                .totalAmount(BigDecimal.valueOf(190))
                .shippingCost(BigDecimal.valueOf(10))
                .discountAmount(BigDecimal.valueOf(20))
                .build();

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);
        when(couponRepository.save(any(CouponEntity.class))).thenReturn(coupon);

        OrderResponseDTO responseDTO = OrderResponseDTO.builder()
                .id(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .shippingCost(savedOrder.getShippingCost())
                .discountAmount(savedOrder.getDiscountAmount())
                .build();

        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.createOrder(orderCreate, clientId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(savedOrder.getId());
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(190));

        verify(userRepository).findById(clientId);
        verify(addressRepository).findById(addressId);
        verify(couponRepository).findByCode("TEST10");
        verify(productRepository).findById(productId);
        verify(productRepository).save(product);
        verify(couponRepository).save(coupon);
        verify(orderRepository).save(any(OrderEntity.class));
        verify(orderMapper).toResponseDTO(savedOrder);

        assertThat(product.getStockQuantity()).isEqualTo(8);
        assertThat(coupon.getCurrentUses()).isEqualTo(1);
    }

    @Test
    void createOrder_success_withoutCoupon_shouldCalculateCorrectly() {
        UUID clientId = client.getId();
        UUID addressId = address.getId();
        UUID productId = product.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(1);

        OrderCreateDTO orderCreate = new OrderCreateDTO();
        orderCreate.setItems(List.of(itemDTO));
        orderCreate.setAddressId(addressId);
        orderCreate.setCouponCode(null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(product);

        OrderEntity savedOrder = OrderEntity.builder()
                .id(UUID.randomUUID())
                .client(client)
                .address(address)
                .coupon(null)
                .status(OrderStatus.WAITING_PAYMENT)
                .totalAmount(BigDecimal.valueOf(113))
                .shippingCost(BigDecimal.valueOf(13))
                .discountAmount(BigDecimal.ZERO)
                .build();

        when(orderRepository.save(any(OrderEntity.class))).thenReturn(savedOrder);

        OrderResponseDTO responseDTO = OrderResponseDTO.builder()
                .id(savedOrder.getId())
                .totalAmount(savedOrder.getTotalAmount())
                .build();

        when(orderMapper.toResponseDTO(savedOrder)).thenReturn(responseDTO);

        OrderResponseDTO result = orderService.createOrder(orderCreate, clientId);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.valueOf(113));

        verify(couponRepository, never()).findByCode(any());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void createOrder_userNotFound_shouldThrow() {
        UUID clientId = UUID.randomUUID();
        when(userRepository.findById(clientId)).thenReturn(Optional.empty());

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(UUID.randomUUID());

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(clientId.toString());

        verify(userRepository).findById(clientId);
        verifyNoMoreInteractions(addressRepository, productRepository);
    }

    @Test
    void createOrder_addressNotFound_shouldThrow() {
        UUID clientId = client.getId();
        UUID addressId = UUID.randomUUID();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.empty());

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(AddressNotFoundException.class)
                .hasMessageContaining(addressId.toString());

        verify(userRepository).findById(clientId);
        verify(addressRepository).findById(addressId);
    }

    @Test
    void createOrder_couponNotFound_shouldThrow() {
        UUID clientId = client.getId();
        UUID addressId = address.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setCouponCode("INVALID");
        dto.setItems(List.of());

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining("INVALID");

        verify(couponRepository).findByCode("INVALID");
    }

    @Test
    void createOrder_couponInactive_shouldThrow() {
        coupon.setActive(false);

        UUID clientId = client.getId();
        UUID addressId = address.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setCouponCode("TEST10");
        dto.setItems(List.of());

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("não está ativo");
    }

    @Test
    void createOrder_couponExpired_shouldThrow() {
        coupon.setValidUntil(LocalDateTime.now().minusDays(1));

        UUID clientId = client.getId();
        UUID addressId = address.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setCouponCode("TEST10");
        dto.setItems(List.of());

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("período de validade");
    }

    @Test
    void createOrder_couponMaxUsesReached_shouldThrow() {
        coupon.setMaxTotalUses(5);
        coupon.setCurrentUses(5);

        UUID clientId = client.getId();
        UUID addressId = address.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setCouponCode("TEST10");
        dto.setItems(List.of());

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(InvalidCouponException.class)
                .hasMessageContaining("limite máximo de usos");
    }

    @Test
    void createOrder_productNotFound_shouldThrow() {
        UUID clientId = client.getId();
        UUID addressId = address.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        UUID productId = UUID.randomUUID();
        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(1);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setItems(List.of(itemDTO));

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(productId.toString());

        verify(productRepository).findById(productId);
    }

    @Test
    void createOrder_productInactive_shouldThrow() {
        product.setActive(false);

        UUID clientId = client.getId();
        UUID addressId = address.getId();
        UUID productId = product.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(1);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setItems(List.of(itemDTO));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(ProductInactiveException.class)
                .hasMessageContaining(product.getName());

        verify(productRepository).findById(productId);
    }

    @Test
    void createOrder_insufficientStock_shouldThrow() {
        product.setStockQuantity(1);

        UUID clientId = client.getId();
        UUID addressId = address.getId();
        UUID productId = product.getId();

        when(userRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        OrderItemCreateDTO itemDTO = new OrderItemCreateDTO();
        itemDTO.setProductId(productId);
        itemDTO.setQuantity(10);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addressId);
        dto.setItems(List.of(itemDTO));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> orderService.createOrder(dto, clientId))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Estoque insuficiente");

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
        assertThat(resp.getStatus()).isEqualTo(OrderStatus.PAID);
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
    void cancelOrder_success_restoresStockAndCouponAndCancels() {
        UUID id = UUID.randomUUID();
        ProductEntity p = ProductEntity.builder().id(UUID.randomUUID()).stockQuantity(5).build();
        OrderItemEntity item = OrderItemEntity.builder().product(p).quantity(2).build();

        OrderEntity order = OrderEntity.builder()
                .id(id)
                .status(OrderStatus.WAITING_PAYMENT)
                .items(List.of(item))
                .coupon(coupon)
                .build();

        coupon.setCurrentUses(5);

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancelOrder(id);

        verify(orderRepository).findById(id);
        verify(productRepository).save(p);
        verify(couponRepository).save(coupon);
        verify(orderRepository).save(order);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(p.getStockQuantity()).isEqualTo(7);
        assertThat(coupon.getCurrentUses()).isEqualTo(4);
    }

    @Test
    void cancelOrder_withoutCoupon_success() {
        UUID id = UUID.randomUUID();
        ProductEntity p = ProductEntity.builder().id(UUID.randomUUID()).stockQuantity(5).build();
        OrderItemEntity item = OrderItemEntity.builder().product(p).quantity(2).build();

        OrderEntity order = OrderEntity.builder()
                .id(id)
                .status(OrderStatus.WAITING_PAYMENT)
                .items(List.of(item))
                .coupon(null)
                .build();

        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancelOrder(id);

        verify(orderRepository).findById(id);
        verify(productRepository).save(p);
        verify(couponRepository, never()).save(any());
        verify(orderRepository).save(order);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(p.getStockQuantity()).isEqualTo(7);
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
    void cancelOrder_illegalState_delivered_shouldThrow() {
        UUID id = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(id).status(OrderStatus.DELIVERED).build();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é possível cancelar");

        verify(orderRepository).findById(id);
    }

    @Test
    void cancelOrder_illegalState_cancelled_shouldThrow() {
        UUID id = UUID.randomUUID();
        OrderEntity order = OrderEntity.builder().id(id).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(id))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Não é possível cancelar");

        verify(orderRepository).findById(id);
    }
}