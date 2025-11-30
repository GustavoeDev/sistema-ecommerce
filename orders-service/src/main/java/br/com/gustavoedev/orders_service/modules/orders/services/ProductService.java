package br.com.gustavoedev.orders_service.modules.orders.services;

import br.com.gustavoedev.orders_service.exceptions.CategoryNotFoundException;
import br.com.gustavoedev.orders_service.exceptions.ProductAlreadyExistsException;
import br.com.gustavoedev.orders_service.exceptions.ProductNotFoundException;
import br.com.gustavoedev.orders_service.modules.orders.dtos.product.ProductCreateDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.product.ProductResponseDTO;
import br.com.gustavoedev.orders_service.modules.orders.dtos.product.ProductUpdateDTO;
import br.com.gustavoedev.orders_service.modules.orders.mappers.ProductMapper;
import br.com.gustavoedev.orders_service.modules.orders.models.CategoryEntity;
import br.com.gustavoedev.orders_service.modules.orders.models.ProductEntity;
import br.com.gustavoedev.orders_service.modules.orders.repositories.CategoryRepository;
import br.com.gustavoedev.orders_service.modules.orders.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    public ProductResponseDTO createProduct(ProductCreateDTO dto) {
        if (productRepository.findByName(dto.getName()) != null) {
            throw new ProductAlreadyExistsException(
                "Produto com nome '" + dto.getName() + "' já existe."
            );
        }

        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Cateogoria com id " + dto.getCategoryId() + " não encontrada."));

        ProductEntity entity = new ProductEntity();
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStockQuantity(dto.getStockQuantity());
        entity.setActive(true);
        entity.setCategory(category);

        ProductEntity saved = productRepository.save(entity);
        return productMapper.toResponseDTO(saved);
    }

    public ProductResponseDTO getProductById(UUID id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto com o id " + id + " não encontrado."));

        return productMapper.toResponseDTO(entity);
    }

    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ProductResponseDTO> getProductsByCategory(UUID categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Cateogoria com id " + categoryId + " não encontrada.");
        }

        return productRepository.findByCategoryId(categoryId).stream()
                .map(productMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO updateProduct(UUID id, ProductUpdateDTO dto) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Produto com o id " + id + " não encontrado."));

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }

        if (dto.getStockQuantity() != null) {
            entity.setStockQuantity(dto.getStockQuantity());
        }

        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }

        if (dto.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Cateogoria com id " + dto.getCategoryId() + " não encontrada."));
            entity.setCategory(category);
        }

        ProductEntity updated = productRepository.save(entity);
        return productMapper.toResponseDTO(updated);
    }

    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Produto com o id " + id + " não encontrado.");
        }
        productRepository.deleteById(id);
    }
}