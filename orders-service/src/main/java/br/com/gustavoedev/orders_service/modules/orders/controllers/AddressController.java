package br.com.gustavoedev.orders_service.modules.orders.controllers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.address.AddressUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.services.AddressService;
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
@RequestMapping("/api/addresses")
@Tag(name = "Endereços", description = "Gerenciamento de endereços dos usuários")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/user/{userId}")
    @Operation(summary = "Cria um novo endereço para um usuário")
    public ResponseEntity<AddressResponseDTO> createAddress(
            @PathVariable UUID userId,
            @Valid @RequestBody AddressCreateDTO dto) {
        AddressResponseDTO response = addressService.createAddress(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um endereço por ID")
    public ResponseEntity<AddressResponseDTO> getAddressById(@PathVariable UUID id) {
        AddressResponseDTO response = addressService.getAddressById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtém todos os endereços de um usuário")
    public ResponseEntity<List<AddressResponseDTO>> getAddressesByUserId(@PathVariable UUID userId) {
        List<AddressResponseDTO> response = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um endereço existente")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressUpdateDTO dto) {
        AddressResponseDTO response = addressService.updateAddress(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um endereço")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }
}