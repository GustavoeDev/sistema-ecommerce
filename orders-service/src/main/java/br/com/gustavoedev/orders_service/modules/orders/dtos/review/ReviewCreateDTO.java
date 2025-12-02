package br.com.gustavoedev.orders_service.modules.orders.dtos.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateDTO {

    @NotNull(message = "ID do produto é obrigatório!")
    private UUID productId;

    @NotNull(message = "Avaliação é obrigatória!")
    @Min(value = 1, message = "Avaliação deve ser entre 1 e 5.")
    @Max(value = 5, message = "Avaliação deve ser entre 1 e 5.")
    private Integer rating;

    @Size(max = 1000, message = "Comentário não pode exceder 1000 caracteres.")
    private String comment;

}