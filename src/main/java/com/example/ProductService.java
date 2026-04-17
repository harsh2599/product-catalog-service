package com.example;

import com.example.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private List<Product> products = new ArrayList<>();

    public ProductService() {
        // Adding sample products
        products.add(new Product(1L, "Laptop", 1200.00));
        products.add(new Product(2L, "Mouse", 25.50));
        products.add(new Product(3L, "Keyboard", 80.00));
        products.add(new Product(4L, "Monitor", 300.00));
        products.add(new Product(5L, "USB Cable", 15.00));
        products.add(new Product());
    }

    /**
     * Filters products based on a minimum price threshold.
     */
    public List<Product> getProductsAbovePrice(double price) {
        return products.stream()
                .filter(p -> p.getPrice() > price)
                .toList();
    }

    /**
     * Extracts only the names of all products.
     * Uses Optional to handle potential null list safely.
     */
    public List<String> getAllProductNames() {
        return Optional.ofNullable(products)
                .orElse(new ArrayList<>())
                .stream()
                .map(Product::getName)
                .filter(p -> p != null )
                .toList();
    }
}