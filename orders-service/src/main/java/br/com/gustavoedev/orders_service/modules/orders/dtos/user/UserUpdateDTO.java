package br.com.gustavoedev.orders_service.modules.orders.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    private String name;

    @Email(message = "O e-mail deve ser válido.")
    private String email;

    private Boolean active;

    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres.")
    private String password;

}