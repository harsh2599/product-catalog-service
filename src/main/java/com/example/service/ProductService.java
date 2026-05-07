package com.example.service;

import com.example.model.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    public List<Product> getAllProducts() {
        return List.of(
            new Product(1L, "Laptop", 1200.00),
            new Product(2L, "Mouse", 25.50),
            new Product(3L, "Keyboard", 80.00),
            new Product(4L, "Monitor", 300.00),
            new Product(5L, "USB Cable", 15.00)
        );
    }
}
