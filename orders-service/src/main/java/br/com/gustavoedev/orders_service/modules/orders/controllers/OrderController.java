package br.com.gustavoedev.orders_service.modules.orders.controllers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.OrderResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.order.UpdateOrderStatusDTO;
import br.com.gustavoedev.orders_service.modules.orders.enums.OrderStatus;
import br.com.gustavoedev.orders_service.modules.orders.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Pedidos", description = "Gerenciamento de pedidos")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/client/{clientId}")
    @Operation(summary = "Cria um novo pedido para um cliente específico")
    public ResponseEntity<OrderResponseDTO> createOrder(@PathVariable UUID clientId, @Valid @RequestBody OrderCreateDTO dto) {
        OrderResponseDTO response = orderService.createOrder(dto, clientId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Obtém pedidos com filtros opcionais")
    public ResponseEntity<?> getOrders(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.getOrders(id, clientId, status));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualiza o status de um pedido existente")
    public ResponseEntity<OrderResponseDTO> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusDTO dto) {

        OrderResponseDTO response = orderService.updateOrderStatus(id, dto.getStatus());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancela um pedido existente")
    public ResponseEntity<Void> cancelOrder(@PathVariable UUID id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}