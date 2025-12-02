package br.com.gustavoedev.orders_service.modules.orders.dtos.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewUpdateDTO {

    @Min(value = 1, message = "Avaliação deve ser entre 1 e 5.")
    @Max(value = 5, message = "Avaliação deve ser entre 1 e 5.")
    private Integer rating;

    @Size(max = 1000, message = "Comentário não pode exceder 1000 caracteres.")
    private String comment;

}