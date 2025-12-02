package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.*;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import br.com.gustavoedev.orders_service.modules.orders.mappers.OrderMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.*;
import br.com.gustavoedev.orders_service.modules.orders.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateDTO dto, UUID clientId) {
        UserEntity client = userRepository.findById(clientId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com id: " + clientId + " não encontrado!"));

        AddressEntity address = addressRepository.findById(dto.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException("Endereço com id: " + dto.getAddressId() + " não encontrado!"));

        CouponEntity coupon = null;
        if (dto.getCouponCode() != null && !dto.getCouponCode().isBlank()) {
            coupon = couponRepository.findByCode(dto.getCouponCode())
                    .orElseThrow(() -> new CouponNotFoundException("Cupom '" + dto.getCouponCode() + "' não encontrado!"));

            validateCoupon(coupon);
        }

        OrderEntity order = new OrderEntity();
        order.setClient(client);
        order.setAddress(address);
        order.setCoupon(coupon);
        order.setStatus(OrderStatus.WAITING_PAYMENT);

        List<OrderItemEntity> items = dto.getItems().stream().map(itemDTO -> {
            ProductEntity product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Produto com o id: " + itemDTO.getProductId() + " não encontrado!"));

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

        BigDecimal subtotal = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingCost = calculateShipping(address.getPostalCode(), items);
        BigDecimal discountAmount = calculateDiscount(coupon, subtotal);
        BigDecimal totalAmount = subtotal.add(shippingCost).subtract(discountAmount);

        order.setShippingCost(shippingCost);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);

        if (coupon != null) {
            coupon.setCurrentUses(coupon.getCurrentUses() + 1);
            couponRepository.save(coupon);
        }

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
                .orElseThrow(() -> new OrderNotFoundException("Pedido com o id: " + id + " não encontrado!"));

        entity.setStatus(newStatus);
        OrderEntity updated = orderRepository.save(entity);

        return orderMapper.toResponseDTO(updated);
    }

    @Transactional
    public void cancelOrder(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido com o id: " + id + " não encontrado!"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Não é possível cancelar o pedido com status: " + order.getStatus());
        }

        order.getItems().forEach(item -> {
            ProductEntity product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        });

        if (order.getCoupon() != null) {
            CouponEntity coupon = order.getCoupon();
            coupon.setCurrentUses(coupon.getCurrentUses() - 1);
            couponRepository.save(coupon);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private void validateCoupon(CouponEntity coupon) {
        if (!coupon.getActive()) {
            throw new InvalidCouponException("Cupom não está ativo.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new InvalidCouponException("Cupom fora do período de validade.");
        }

        if (coupon.getMaxTotalUses() != null && coupon.getCurrentUses() >= coupon.getMaxTotalUses()) {
            throw new InvalidCouponException("Cupom atingiu o limite máximo de usos.");
        }
    }

    private BigDecimal calculateDiscount(CouponEntity coupon, BigDecimal subtotal) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }

        if (coupon.getMinimumPurchaseAmount() != null && subtotal.compareTo(coupon.getMinimumPurchaseAmount()) < 0) {
            throw new InvalidCouponException("Valor mínimo de compra não atingido para usar este cupom.");
        }

        if (coupon.getDiscountPercentage() != null) {
            return subtotal.multiply(coupon.getDiscountPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        if (coupon.getDiscountFixed() != null) {
            return coupon.getDiscountFixed();
        }

        return BigDecimal.ZERO;
    }

    private BigDecimal calculateShipping(String postalCode, List<OrderItemEntity> items) {
        BigDecimal totalWeight = items.stream()
                .map(item -> item.getProduct().getWeight() != null ?
                    item.getProduct().getWeight().multiply(BigDecimal.valueOf(item.getQuantity())) :
                    BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BigDecimal.valueOf(10).add(totalWeight.multiply(BigDecimal.valueOf(2)));
    }
}