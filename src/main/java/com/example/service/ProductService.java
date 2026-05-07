package com.example.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.dto.ProductDTO;
import com.example.dto.ReviewDTO;
import com.example.exception.ProductNotFoundException;
import com.example.model.Product;
import com.example.model.Review;
import com.example.repository.ProductRepository;
import com.example.repository.ReviewRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    public Page<Product> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product addProduct(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        return productRepository.save(product);
    }

    public Review addReview(Long productId, ReviewDTO dto) {
        Product product = getProductById(productId);
        Review review = new Review();
        review.setComment(dto.getComment());
        review.setProduct(product);
        return reviewRepository.save(review);
    }
}
