package br.com.gustavoedev.orders_service.modules.orders.dtos.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateDTO {

    @NotBlank(message = "CEP é obrigatório!")
    @Size(max = 10, message = "CEP não pode exceder 10 caracteres.")
    private String postalCode;

    @NotBlank(message = "Rua é obrigatória!")
    @Size(max = 200, message = "Rua não pode exceder 200 caracteres.")
    private String street;

    @NotBlank(message = "Número é obrigatório!")
    @Size(max = 20, message = "Número não pode exceder 20 caracteres.")
    private String number;

    @Size(max = 100, message = "Complemento não pode exceder 100 caracteres.")
    private String complement;

    @NotBlank(message = "Bairro é obrigatório!")
    @Size(max = 100, message = "Bairro não pode exceder 100 caracteres.")
    private String neighborhood;

    @NotBlank(message = "Cidade é obrigatória!")
    @Size(max = 100, message = "Cidade não pode exceder 100 caracteres.")
    private String city;

    @NotBlank(message = "Estado é obrigatório!")
    @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres (UF).")
    private String state;

    @Builder.Default
    private Boolean isDefault = false;

}