package br.com.gustavoedev.orders_service.modules.orders.controllers;

import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreateDTO dto) {
        CategoryResponseDTO response = categoryService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable UUID id) {
        CategoryResponseDTO response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> response = categoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateDTO dto) {
        CategoryResponseDTO response = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}