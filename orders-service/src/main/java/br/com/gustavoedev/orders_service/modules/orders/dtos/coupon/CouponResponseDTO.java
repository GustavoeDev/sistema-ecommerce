package br.com.gustavoedev.orders_service.modules.orders.dtos.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponseDTO {

    private UUID id;
    private String code;
    private BigDecimal discountPercentage;
    private BigDecimal discountFixed;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Integer maxUsesPerCustomer;
    private Integer maxTotalUses;
    private Integer currentUses;
    private Boolean active;
    private BigDecimal minimumPurchaseAmount;

}