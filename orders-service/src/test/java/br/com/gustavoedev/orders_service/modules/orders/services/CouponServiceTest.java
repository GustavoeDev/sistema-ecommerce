package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.CouponAlreadyExistsException;
import br.com.gustavoedev.orders_service.exceptions.CouponNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.coupon.CouponUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.CouponMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.CouponEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.CouponRepository;
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
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponService couponService;

    private CouponEntity coupon;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void createCoupon_success() {
        CouponCreateDTO dto = new CouponCreateDTO();
        dto.setCode("NEW10");
        dto.setDiscountPercentage(BigDecimal.valueOf(10));
        dto.setValidFrom(LocalDateTime.now());
        dto.setValidUntil(LocalDateTime.now().plusDays(7));
        dto.setMaxTotalUses(50);

        when(couponRepository.findByCode(dto.getCode())).thenReturn(Optional.empty());
        when(couponRepository.save(any())).thenReturn(coupon);
        when(couponMapper.toResponseDTO(coupon)).thenReturn(
                CouponResponseDTO.builder().code("NEW10").build()
        );

        CouponResponseDTO result = couponService.createCoupon(dto);

        assertThat(result).isNotNull();
        verify(couponRepository).findByCode(dto.getCode());
        verify(couponRepository).save(any());
    }

    @Test
    void createCoupon_codeAlreadyExists_shouldThrow() {
        CouponCreateDTO dto = new CouponCreateDTO();
        dto.setCode("EXISTING");

        when(couponRepository.findByCode(dto.getCode())).thenReturn(Optional.of(coupon));

        assertThatThrownBy(() -> couponService.createCoupon(dto))
                .isInstanceOf(CouponAlreadyExistsException.class)
                .hasMessageContaining("EXISTING");

        verify(couponRepository).findByCode(dto.getCode());
        verify(couponRepository, never()).save(any());
    }

    @Test
    void getCouponById_success() {
        UUID id = coupon.getId();
        when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));
        when(couponMapper.toResponseDTO(coupon)).thenReturn(
                CouponResponseDTO.builder().id(id).build()
        );

        CouponResponseDTO result = couponService.getCouponById(id);

        assertThat(result).isNotNull();
        verify(couponRepository).findById(id);
    }

    @Test
    void getCouponById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(couponRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getCouponById(id))
                .isInstanceOf(CouponNotFoundException.class);

        verify(couponRepository).findById(id);
    }

    @Test
    void getCouponByCode_success() {
        when(couponRepository.findByCode("TEST10")).thenReturn(Optional.of(coupon));
        when(couponMapper.toResponseDTO(coupon)).thenReturn(
                CouponResponseDTO.builder().code("TEST10").build()
        );

        CouponResponseDTO result = couponService.getCouponByCode("TEST10");

        assertThat(result).isNotNull();
        verify(couponRepository).findByCode("TEST10");
    }

    @Test
    void getAllCoupons_success() {
        when(couponRepository.findAll()).thenReturn(List.of(coupon));
        when(couponMapper.toResponseDTO(coupon)).thenReturn(
                CouponResponseDTO.builder().code("TEST10").build()
        );

        List<CouponResponseDTO> result = couponService.getAllCoupons();

        assertThat(result).hasSize(1);
        verify(couponRepository).findAll();
    }

    @Test
    void updateCoupon_success() {
        UUID id = coupon.getId();
        CouponUpdateDTO dto = new CouponUpdateDTO();
        dto.setDiscountPercentage(BigDecimal.valueOf(15));
        dto.setActive(false);

        when(couponRepository.findById(id)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toResponseDTO(coupon)).thenReturn(
                CouponResponseDTO.builder().id(id).build()
        );

        CouponResponseDTO result = couponService.updateCoupon(id, dto);

        assertThat(result).isNotNull();
        verify(couponRepository).findById(id);
        verify(couponRepository).save(coupon);
    }

    @Test
    void deleteCoupon_success() {
        UUID id = coupon.getId();
        when(couponRepository.existsById(id)).thenReturn(true);

        couponService.deleteCoupon(id);

        verify(couponRepository).existsById(id);
        verify(couponRepository).deleteById(id);
    }

    @Test
    void deleteCoupon_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(couponRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> couponService.deleteCoupon(id))
                .isInstanceOf(CouponNotFoundException.class);

        verify(couponRepository).existsById(id);
        verify(couponRepository, never()).deleteById(any());
    }
}