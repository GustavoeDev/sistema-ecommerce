package br.com.gustavoedev.orders_service.modules.orders.controllers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.review.ReviewUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.services.ReviewService;
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
@RequestMapping("/api/reviews")
@Tag(name = "Avaliações", description = "Gerenciamento de avaliações de produtos")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/user/{userId}")
    @Operation(summary = "Cria uma nova avaliação")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable UUID userId,
            @Valid @RequestBody ReviewCreateDTO dto) {
        ReviewResponseDTO response = reviewService.createReview(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém uma avaliação por ID")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable UUID id) {
        ReviewResponseDTO response = reviewService.getReviewById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Obtém todas as avaliações de um produto")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByProductId(@PathVariable UUID productId) {
        List<ReviewResponseDTO> response = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtém todas as avaliações de um usuário")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUserId(@PathVariable UUID userId) {
        List<ReviewResponseDTO> response = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma avaliação existente")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewUpdateDTO dto) {
        ReviewResponseDTO response = reviewService.updateReview(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma avaliação")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}