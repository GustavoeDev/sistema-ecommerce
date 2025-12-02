package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.CouponAlreadyExistsException;
import br.com.gustavoedev.orders_service.exceptions.CouponNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.CouponMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.CouponEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public CouponResponseDTO createCoupon(CouponCreateDTO dto) {
        couponRepository.findByCode(dto.getCode())
            .ifPresent(coupon -> {
                throw new CouponAlreadyExistsException(
                    "Cupom com código '" + dto.getCode() + "' já existe!"
                );
            });

        CouponEntity entity = CouponEntity.builder()
                .code(dto.getCode())
                .discountPercentage(dto.getDiscountPercentage())
                .discountFixed(dto.getDiscountFixed())
                .validFrom(dto.getValidFrom())
                .validUntil(dto.getValidUntil())
                .maxUsesPerCustomer(dto.getMaxUsesPerCustomer())
                .maxTotalUses(dto.getMaxTotalUses())
                .currentUses(0)
                .active(true)
                .minimumPurchaseAmount(dto.getMinimumPurchaseAmount())
                .build();

        CouponEntity saved = couponRepository.save(entity);
        return couponMapper.toResponseDTO(saved);
    }

    public CouponResponseDTO getCouponById(UUID id) {
        CouponEntity entity = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Cupom com id: " + id + " não encontrado!"));

        return couponMapper.toResponseDTO(entity);
    }

    public CouponResponseDTO getCouponByCode(String code) {
        CouponEntity entity = couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException("Cupom com código: " + code + " não encontrado!"));

        return couponMapper.toResponseDTO(entity);
    }

    public List<CouponResponseDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CouponResponseDTO updateCoupon(UUID id, CouponUpdateDTO dto) {
        CouponEntity entity = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Cupom com id: " + id + " não encontrado!"));

        if (dto.getDiscountPercentage() != null) {
            entity.setDiscountPercentage(dto.getDiscountPercentage());
        }

        if (dto.getDiscountFixed() != null) {
            entity.setDiscountFixed(dto.getDiscountFixed());
        }

        if (dto.getValidFrom() != null) {
            entity.setValidFrom(dto.getValidFrom());
        }

        if (dto.getValidUntil() != null) {
            entity.setValidUntil(dto.getValidUntil());
        }

        if (dto.getMaxUsesPerCustomer() != null) {
            entity.setMaxUsesPerCustomer(dto.getMaxUsesPerCustomer());
        }

        if (dto.getMaxTotalUses() != null) {
            entity.setMaxTotalUses(dto.getMaxTotalUses());
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        if (dto.getMinimumPurchaseAmount() != null) {
            entity.setMinimumPurchaseAmount(dto.getMinimumPurchaseAmount());
        }

        CouponEntity updated = couponRepository.save(entity);
        return couponMapper.toResponseDTO(updated);
    }

    public void deleteCoupon(UUID id) {
        if (!couponRepository.existsById(id)) {
            throw new CouponNotFoundException("Cupom com id: " + id + " não encontrado!");
        }
        couponRepository.deleteById(id);
    }
}