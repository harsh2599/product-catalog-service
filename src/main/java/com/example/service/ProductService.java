package com.example.service;

import com.example.dto.ProductDTO;
import com.example.exception.ProductNotFoundException;
import com.example.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>(List.of(
            new Product(1L, "Laptop", 1200.00),
            new Product(2L, "Mouse", 25.50),
            new Product(3L, "Keyboard", 80.00),
            new Product(4L, "Monitor", 300.00),
            new Product(5L, "USB Cable", 15.00)
    ));

    private final AtomicLong idSequence = new AtomicLong(6);

    public List<Product> getAllProducts() {
        return List.copyOf(products);
    }

    public Product getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product addProduct(ProductDTO dto) {
        Product product = new Product(idSequence.getAndIncrement(), dto.getName(), dto.getPrice());
        products.add(product);
        return product;
    }
}
