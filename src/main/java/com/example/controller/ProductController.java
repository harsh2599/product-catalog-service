package com.example.controller;

import com.example.dto.ProductDTO;
import com.example.dto.ReviewDTO;
import com.example.model.Product;
import com.example.model.Review;
import com.example.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getAllProducts(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody @Valid ProductDTO dto) {
        Product created = productService.addProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<Review> addReview(
            @PathVariable Long id,
            @RequestBody @Valid ReviewDTO dto) {
        Review created = productService.addReview(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
