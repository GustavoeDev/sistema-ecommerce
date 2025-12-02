package br.com.gustavoedev.orders_service.modules.orders.controllers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.services.CouponService;
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
@RequestMapping("/api/coupons")
@Tag(name = "Cupons", description = "Gerenciamento de cupons de desconto")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @Operation(summary = "Cria um novo cupom")
    public ResponseEntity<CouponResponseDTO> createCoupon(@Valid @RequestBody CouponCreateDTO dto) {
        CouponResponseDTO response = couponService.createCoupon(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um cupom por ID")
    public ResponseEntity<CouponResponseDTO> getCouponById(@PathVariable UUID id) {
        CouponResponseDTO response = couponService.getCouponById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtém um cupom por código")
    public ResponseEntity<CouponResponseDTO> getCouponByCode(@PathVariable String code) {
        CouponResponseDTO response = couponService.getCouponByCode(code);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obtém todos os cupons")
    public ResponseEntity<List<CouponResponseDTO>> getAllCoupons() {
        List<CouponResponseDTO> response = couponService.getAllCoupons();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um cupom existente")
    public ResponseEntity<CouponResponseDTO> updateCoupon(
            @PathVariable UUID id,
            @Valid @RequestBody CouponUpdateDTO dto) {
        CouponResponseDTO response = couponService.updateCoupon(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um cupom")
    public ResponseEntity<Void> deleteCoupon(@PathVariable UUID id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}