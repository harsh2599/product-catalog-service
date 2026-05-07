package com.example.service;

import com.example.dto.ProductDTO;
import com.example.dto.ProductWithReviewsDTO;
import com.example.dto.ReviewDTO;
import com.example.exception.EmptyReviewsException;
import com.example.exception.ProductNotFoundException;
import com.example.model.Product;
import com.example.model.Review;
import com.example.repository.ProductRepository;
import com.example.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    // -------------------------------------------------------------------------
    // getAllProducts
    // -------------------------------------------------------------------------

    @Test
    void getAllProducts_returnsPageFromRepository() {
        Product laptop = productWithId(1L, "Laptop", 1200.0);
        Product mouse  = productWithId(2L, "Mouse", 25.5);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> fakePage = new PageImpl<>(List.of(laptop, mouse), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(fakePage);

        Page<Product> result = productService.getAllProducts(0, 10);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(Product::getName)
                .containsExactly("Laptop", "Mouse");
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getAllProducts_respectsPageAndSizeParameters() {
        PageRequest pageable = PageRequest.of(1, 2);
        Page<Product> fakePage = new PageImpl<>(List.of(), pageable, 5);

        when(productRepository.findAll(pageable)).thenReturn(fakePage);

        Page<Product> result = productService.getAllProducts(1, 2);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(2);
        verify(productRepository).findAll(PageRequest.of(1, 2));
    }

    // -------------------------------------------------------------------------
    // getProductById
    // -------------------------------------------------------------------------

    @Test
    void getProductById_returnsProduct_whenFound() {
        Product laptop = productWithId(1L, "Laptop", 1200.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        Product result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_throwsProductNotFoundException_whenNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product not found with id: 99");

        verify(productRepository).findById(99L);
    }

    // -------------------------------------------------------------------------
    // addProduct
    // -------------------------------------------------------------------------

    @Test
    void addProduct_savesAndReturnsNewProduct() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Keyboard");
        dto.setPrice(80.0);

        Product saved = productWithId(3L, "Keyboard", 80.0);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        Product result = productService.addProduct(dto);

        assertThat(result.getName()).isEqualTo("Keyboard");
        assertThat(result.getPrice()).isEqualTo(80.0);
        verify(productRepository).save(any(Product.class));
    }

    // -------------------------------------------------------------------------
    // addReview
    // -------------------------------------------------------------------------

    @Test
    void addReview_savesReviewLinkedToProduct() {
        Product laptop = productWithId(1L, "Laptop", 1200.0);
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        Review saved = new Review();
        saved.setId(1L);
        saved.setComment("Great laptop!");
        saved.setProduct(laptop);
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewDTO dto = new ReviewDTO();
        dto.setComment("Great laptop!");

        Review result = productService.addReview(1L, dto);

        assertThat(result.getComment()).isEqualTo("Great laptop!");
        assertThat(result.getProduct()).isEqualTo(laptop);
        verify(productRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void addReview_throwsProductNotFoundException_whenProductMissing() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        ReviewDTO dto = new ReviewDTO();
        dto.setComment("Irrelevant");

        assertThatThrownBy(() -> productService.addReview(42L, dto))
                .isInstanceOf(ProductNotFoundException.class);

        verify(reviewRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // createProductWithReviews
    // -------------------------------------------------------------------------

    @Test
    void createProductWithReviews_savesProductAndAllReviews() {
        ProductWithReviewsDTO dto = new ProductWithReviewsDTO();
        dto.setName("Monitor");
        dto.setPrice(300.0);
        ReviewDTO r1 = new ReviewDTO(); r1.setComment("Sharp display");
        ReviewDTO r2 = new ReviewDTO(); r2.setComment("Great value");
        dto.setReviews(List.of(r1, r2));

        Product saved = productWithId(5L, "Monitor", 300.0);
        when(productRepository.save(any(Product.class))).thenReturn(saved);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.createProductWithReviews(dto);

        assertThat(result.getReviews()).hasSize(2)
                .extracting(Review::getComment)
                .containsExactly("Sharp display", "Great value");
        verify(productRepository).save(any(Product.class));
        verify(reviewRepository, times(2)).save(any(Review.class));
    }

    @Test
    void createProductWithReviews_throwsEmptyReviewsException_andRollsBack_whenReviewsEmpty() {
        ProductWithReviewsDTO dto = new ProductWithReviewsDTO();
        dto.setName("Ghost Product");
        dto.setPrice(9.99);
        dto.setReviews(List.of());

        when(productRepository.save(any(Product.class))).thenReturn(productWithId(10L, "Ghost Product", 9.99));

        assertThatThrownBy(() -> productService.createProductWithReviews(dto))
                .isInstanceOf(EmptyReviewsException.class)
                .hasMessage("At least one review is required to create a product");

        // Product save was attempted before the check, but no reviews were saved
        verify(productRepository).save(any(Product.class));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createProductWithReviews_throwsEmptyReviewsException_whenReviewsNull() {
        ProductWithReviewsDTO dto = new ProductWithReviewsDTO();
        dto.setName("Ghost Product");
        dto.setPrice(9.99);
        dto.setReviews(null);

        when(productRepository.save(any(Product.class))).thenReturn(productWithId(11L, "Ghost Product", 9.99));

        assertThatThrownBy(() -> productService.createProductWithReviews(dto))
                .isInstanceOf(EmptyReviewsException.class);

        verify(reviewRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // helper
    // -------------------------------------------------------------------------

    private Product productWithId(Long id, String name, double price) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        return p;
    }
}
