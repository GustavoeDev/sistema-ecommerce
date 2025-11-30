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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private CategoryEntity category;

    @BeforeEach
    void setUp() {
        category = CategoryEntity.builder()
                .id(UUID.randomUUID())
                .name("Cat")
                .build();
    }

    @Test
    void createProduct_success() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setName("Prod A");
        dto.setPrice(BigDecimal.valueOf(10));
        dto.setStockQuantity(5);
        dto.setCategoryId(category.getId());

        when(productRepository.findByName(dto.getName())).thenReturn(null);
        when(categoryRepository.findById(dto.getCategoryId())).thenReturn(Optional.of(category));

        ProductEntity saved = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .active(true)
                .category(category)
                .build();

        when(productRepository.save(any())).thenReturn(saved);

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .price(saved.getPrice())
                .stockQuantity(saved.getStockQuantity())
                .active(saved.getActive())
                .build();

        when(productMapper.toResponseDTO(saved)).thenReturn(responseDTO);

        ProductResponseDTO result = productService.createProduct(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo(dto.getName());

        verify(productRepository).findByName(dto.getName());
        verify(categoryRepository).findById(dto.getCategoryId());
        verify(productRepository).save(any(ProductEntity.class));
        verify(productMapper).toResponseDTO(saved);
    }

    @Test
    void createProduct_whenNameAlreadyExists_shouldThrow() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setName("Existing");

        when(productRepository.findByName(dto.getName())).thenReturn(new ProductEntity());

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(ProductAlreadyExistsException.class)
                .hasMessageContaining(dto.getName());

        verify(productRepository).findByName(dto.getName());
        verifyNoMoreInteractions(categoryRepository);
    }

    @Test
    void createProduct_whenCategoryNotFound_shouldThrow() {
        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setName("NewProd");
        dto.setCategoryId(UUID.randomUUID());

        when(productRepository.findByName(dto.getName())).thenReturn(null);
        when(categoryRepository.findById(dto.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(dto))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository).findByName(dto.getName());
        verify(categoryRepository).findById(dto.getCategoryId());
    }

    @Test
    void getProductById_success() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder().name("P").build();

        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        ProductResponseDTO dto = ProductResponseDTO.builder().id(null).name("P").build();
        when(productMapper.toResponseDTO(entity)).thenReturn(dto);

        ProductResponseDTO result = productService.getProductById(id);

        assertThat(result).isEqualTo(dto);
        verify(productRepository).findById(id);
        verify(productMapper).toResponseDTO(entity);
    }

    @Test
    void getProductById_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(id);
    }

    @Test
    void getAllProducts_success_mapsAll() {
        ProductEntity p1 = ProductEntity.builder().name("A").build();
        ProductEntity p2 = ProductEntity.builder().name("B").build();

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(productMapper.toResponseDTO(p1)).thenReturn(ProductResponseDTO.builder().name("A").build());
        when(productMapper.toResponseDTO(p2)).thenReturn(ProductResponseDTO.builder().name("B").build());

        List<ProductResponseDTO> list = productService.getAllProducts();

        assertThat(list).hasSize(2);
        verify(productRepository).findAll();
        verify(productMapper).toResponseDTO(p1);
        verify(productMapper).toResponseDTO(p2);
    }

    @Test
    void getActiveProducts_success() {
        ProductEntity p = ProductEntity.builder().active(true).build();
        when(productRepository.findByActiveTrue()).thenReturn(List.of(p));
        when(productMapper.toResponseDTO(p)).thenReturn(ProductResponseDTO.builder().active(true).build());

        List<ProductResponseDTO> list = productService.getActiveProducts();

        assertThat(list).hasSize(1);
        verify(productRepository).findByActiveTrue();
    }

    @Test
    void getProductsByCategory_success() {
        UUID catId = UUID.randomUUID();
        ProductEntity p = ProductEntity.builder().build();

        when(categoryRepository.existsById(catId)).thenReturn(true);
        when(productRepository.findByCategoryId(catId)).thenReturn(List.of(p));
        when(productMapper.toResponseDTO(p)).thenReturn(ProductResponseDTO.builder().build());

        List<ProductResponseDTO> result = productService.getProductsByCategory(catId);

        assertThat(result).hasSize(1);
        verify(categoryRepository).existsById(catId);
        verify(productRepository).findByCategoryId(catId);
    }

    @Test
    void getProductsByCategory_categoryNotFound_shouldThrow() {
        UUID catId = UUID.randomUUID();
        when(categoryRepository.existsById(catId)).thenReturn(false);

        assertThatThrownBy(() -> productService.getProductsByCategory(catId))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(categoryRepository).existsById(catId);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void updateProduct_success_changeFieldsAndCategory() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder().name("Old").price(BigDecimal.valueOf(1)).stockQuantity(2).active(true).build();

        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setName("New");
        dto.setPrice(BigDecimal.valueOf(5));
        dto.setStockQuantity(10);
        dto.setActive(false);
        UUID newCatId = category.getId();
        dto.setCategoryId(newCatId);

        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(newCatId)).thenReturn(Optional.of(category));

        ProductEntity updated = ProductEntity.builder().name("New").build();
        when(productRepository.save(entity)).thenReturn(updated);
        when(productMapper.toResponseDTO(updated)).thenReturn(ProductResponseDTO.builder().name("New").build());

        var result = productService.updateProduct(id, dto);

        assertThat(result.getName()).isEqualTo("New");

        verify(productRepository).findById(id);
        verify(categoryRepository).findById(newCatId);
        verify(productRepository).save(entity);
        verify(productMapper).toResponseDTO(updated);
    }

    @Test
    void updateProduct_productNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(id, new ProductUpdateDTO()))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).findById(id);
    }

    @Test
    void updateProduct_categoryNotFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder().build();
        ProductUpdateDTO dto = new ProductUpdateDTO();
        dto.setCategoryId(UUID.randomUUID());

        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(categoryRepository.findById(dto.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(id, dto))
                .isInstanceOf(CategoryNotFoundException.class);

        verify(productRepository).findById(id);
        verify(categoryRepository).findById(dto.getCategoryId());
    }

    @Test
    void deleteProduct_success() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(true);

        productService.deleteProduct(id);

        verify(productRepository).existsById(id);
        verify(productRepository).deleteById(id);
    }

    @Test
    void deleteProduct_notFound_shouldThrow() {
        UUID id = UUID.randomUUID();
        when(productRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(id))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository).existsById(id);
        verify(productRepository, never()).deleteById(any());
    }
}
