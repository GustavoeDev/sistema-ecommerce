package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.ProductNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.ReviewNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.UserNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.ReviewMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.ProductEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.ReviewEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.UserEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.ProductRepository;
import br.com.gustavoedev.orders_service.modules.orders.repositories.ReviewRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    private UserEntity user;
    private ProductEntity product;
    private ReviewEntity review;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(UUID.randomUUID())
                .name("John Doe")
                .email("john@test.com")
                .build();

        product = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product Test")
                .averageRating(null)
                .build();

        review = ReviewEntity.builder()
                .id(UUID.randomUUID())
                .user(user)
                .product(product)
                .rating(5)
                .comment("Excellent!")
                .build();
    }

    @Test
    void createReview_success_updatesAverageRating() {
        UUID userId = user.getId();
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setProductId(product.getId());
        dto.setRating(5);
        dto.setComment("Great product!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(reviewRepository.save(any())).thenReturn(review);
        when(reviewRepository.findByProductId(product.getId())).thenReturn(List.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(
                ReviewResponseDTO.builder().id(review.getId()).rating(5).build()
        );

        ReviewResponseDTO result = reviewService.createReview(userId, dto);

        assertThat(result).isNotNull();
        verify(userRepository).findById(userId);
        verify(productRepository, times(2)).findById(product.getId());
        verify(reviewRepository).save(any());
        verify(productRepository).save(product);
        assertThat(product.getAverageRating())
            .usingComparator(BigDecimal::compareTo)
            .isEqualTo(BigDecimal.valueOf(5.00));
    }

    @Test
    void createReview_userNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(userId, new ReviewCreateDTO()))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findById(userId);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_productNotFound_shouldThrow() {
        UUID userId = user.getId();
        ReviewCreateDTO dto = new ReviewCreateDTO();
        dto.setProductId(UUID.randomUUID());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(productRepository.findById(dto.getProductId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.createReview(userId, dto))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(dto.getProductId());
    }

    @Test
    void getReviewById_success() {
        UUID id = review.getId();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(
                ReviewResponseDTO.builder().id(id).build()
        );

        ReviewResponseDTO result = reviewService.getReviewById(id);

        assertThat(result).isNotNull();
        verify(reviewRepository).findById(id);
    }

    @Test
    void getReviewById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(id))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository).findById(id);
    }

    @Test
    void getReviewsByProductId_success() {
        UUID productId = product.getId();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findByProductId(productId)).thenReturn(List.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(
                ReviewResponseDTO.builder().id(review.getId()).build()
        );

        List<ReviewResponseDTO> result = reviewService.getReviewsByProductId(productId);

        assertThat(result).hasSize(1);
        verify(productRepository).existsById(productId);
        verify(reviewRepository).findByProductId(productId);
    }

    @Test
    void getReviewsByProductId_productNotFound_shouldThrow() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByProductId(productId))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).existsById(productId);
    }

    @Test
    void getReviewsByUserId_success() {
        UUID userId = user.getId();
        when(userRepository.existsById(userId)).thenReturn(true);
        when(reviewRepository.findByUserId(userId)).thenReturn(List.of(review));
        when(reviewMapper.toResponseDTO(review)).thenReturn(
                ReviewResponseDTO.builder().id(review.getId()).build()
        );

        List<ReviewResponseDTO> result = reviewService.getReviewsByUserId(userId);

        assertThat(result).hasSize(1);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findByUserId(userId);
    }

    @Test
    void getReviewsByUserId_userNotFound_shouldThrow() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getReviewsByUserId(userId))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).existsById(userId);
    }

    @Test
    void updateReview_success_updatesAverageRating() {
        UUID id = review.getId();
        ReviewUpdateDTO dto = new ReviewUpdateDTO();
        dto.setRating(4);
        dto.setComment("Good product");

        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewRepository.findByProductId(product.getId())).thenReturn(List.of(review));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(reviewMapper.toResponseDTO(review)).thenReturn(
                ReviewResponseDTO.builder().id(id).rating(4).build()
        );

        ReviewResponseDTO result = reviewService.updateReview(id, dto);

        assertThat(result).isNotNull();
        verify(reviewRepository).findById(id);
        verify(reviewRepository).save(review);
        verify(productRepository).save(product);
    }

    @Test
    void updateReview_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(id, new ReviewUpdateDTO()))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository).findById(id);
    }

    @Test
    void deleteReview_success_updatesAverageRating() {
        UUID id = review.getId();
        when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        when(reviewRepository.findByProductId(product.getId())).thenReturn(List.of());
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        reviewService.deleteReview(id);

        verify(reviewRepository).findById(id);
        verify(reviewRepository).deleteById(id);
        verify(productRepository).save(product);
        assertThat(product.getAverageRating()).isNull();
    }

    @Test
    void deleteReview_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(reviewRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(id))
                .isInstanceOf(ReviewNotFoundException.class);

        verify(reviewRepository).findById(id);
        verify(reviewRepository, never()).deleteById(any());
    }
}