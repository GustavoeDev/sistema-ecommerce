package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.CategoryAlreadyExistsException;
import br.com.gustavoedev.orders_service.exceptions.CategoryNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.category.CategoryUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.CategoryMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.CategoryEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponseDTO createCategory(CategoryCreateDTO dto) {
        categoryRepository.findByName(dto.getName())
            .ifPresent(category -> {
                throw new CategoryAlreadyExistsException(
                    "Categoria com nome '" + dto.getName() + "' já existe!"
                );
            });

        CategoryEntity entity = CategoryEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        CategoryEntity saved = categoryRepository.save(entity);
        return categoryMapper.toResponseDTO(saved);
    }

    public CategoryResponseDTO getCategoryById(UUID id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria com id: " + id + " não encontrada!"));

        return categoryMapper.toResponseDTO(entity);
    }

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO updateCategory(UUID id, CategoryUpdateDTO dto) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Categoria com id: " + id + " não encontrada!"));

        if (dto.getName() != null && !dto.getName().equals(entity.getName())) {
            categoryRepository.findByName(dto.getName())
                .ifPresent(category -> {
                    throw new CategoryAlreadyExistsException(
                        "Categoria com nome '" + dto.getName() + "' já existe!"
                    );
                });
            entity.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        CategoryEntity updated = categoryRepository.save(entity);
        return categoryMapper.toResponseDTO(updated);
    }

    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("Categoria com id: " + id + " não encontrada!");
        }
        categoryRepository.deleteById(id);
    }
}