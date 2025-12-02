package br.com.gustavoedev.orders_service.modules.orders.dtos.coupon;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUpdateDTO {

    @DecimalMin(value = "0.01", message = "Desconto percentual deve ser maior que zero!")
    @DecimalMax(value = "100.00", message = "Desconto percentual não pode exceder 100%!")
    private BigDecimal discountPercentage;

    @DecimalMin(value = "0.01", message = "Desconto fixo deve ser maior que zero!")
    private BigDecimal discountFixed;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    @Min(value = 1, message = "Máximo de usos por cliente deve ser pelo menos 1.")
    private Integer maxUsesPerCustomer;

    @Min(value = 1, message = "Máximo total de usos deve ser pelo menos 1.")
    private Integer maxTotalUses;

    private Boolean active;

    @DecimalMin(value = "0.00", message = "Valor mínimo de compra não pode ser negativo!")
    private BigDecimal minimumPurchaseAmount;
}