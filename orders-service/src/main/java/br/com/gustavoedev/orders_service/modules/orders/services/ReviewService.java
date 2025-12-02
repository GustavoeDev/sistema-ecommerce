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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponseDTO createReview(UUID userId, ReviewCreateDTO dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário com id: " + userId + " não encontrado!"));

        ProductEntity product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Produto com id: " + dto.getProductId() + " não encontrado!"));

        ReviewEntity entity = ReviewEntity.builder()
                .user(user)
                .product(product)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .build();

        ReviewEntity saved = reviewRepository.save(entity);

        updateProductAverageRating(product.getId());

        return reviewMapper.toResponseDTO(saved);
    }

    public ReviewResponseDTO getReviewById(UUID id) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Avaliação com id: " + id + " não encontrada!"));

        return reviewMapper.toResponseDTO(entity);
    }

    public List<ReviewResponseDTO> getReviewsByProductId(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Produto com id: " + productId + " não encontrado!");
        }

        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ReviewResponseDTO> getReviewsByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Usuário com id: " + userId + " não encontrado!");
        }

        return reviewRepository.findByUserId(userId).stream()
                .map(reviewMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponseDTO updateReview(UUID id, ReviewUpdateDTO dto) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Avaliação com id: " + id + " não encontrada!"));
        if (dto.getRating() != null) {
            entity.setRating(dto.getRating());
        }

        if (dto.getComment() != null) {
            entity.setComment(dto.getComment());
        }

        ReviewEntity updated = reviewRepository.save(entity);

        updateProductAverageRating(entity.getProduct().getId());

        return reviewMapper.toResponseDTO(updated);
    }

    @Transactional
    public void deleteReview(UUID id) {
        ReviewEntity entity = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Avaliação com id: " + id + " não encontrada!"));

        UUID productId = entity.getProduct().getId();
        reviewRepository.deleteById(id);

        updateProductAverageRating(productId);
    }

    private void updateProductAverageRating(UUID productId) {
        List<ReviewEntity> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado!"));
            product.setAverageRating(null);
            productRepository.save(product);
            return;
        }

        double average = reviews.stream()
                .mapToInt(ReviewEntity::getRating)
                .average()
                .orElse(0.0);

        BigDecimal averageRating = BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Produto não encontrado!"));
        product.setAverageRating(averageRating);
        productRepository.save(product);
    }
}