package br.com.gustavoedev.orders_service.modules.orders.mappers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.models.CouponEntity;
import org.springframework.stereotype.Component;

@Component
public class CouponMapper {

    public CouponResponseDTO toResponseDTO(CouponEntity entity) {
        return CouponResponseDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .discountPercentage(entity.getDiscountPercentage())
                .discountFixed(entity.getDiscountFixed())
                .validFrom(entity.getValidFrom())
                .validUntil(entity.getValidUntil())
                .maxUsesPerCustomer(entity.getMaxUsesPerCustomer())
                .maxTotalUses(entity.getMaxTotalUses())
                .currentUses(entity.getCurrentUses())
                .active(entity.getActive())
                .minimumPurchaseAmount(entity.getMinimumPurchaseAmount())
                .build();
    }
}