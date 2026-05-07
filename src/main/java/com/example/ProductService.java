package com.example;

import com.example.model.Product;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductService {
    private List<Product> products = new ArrayList<>();

    public ProductService() {
        products.add(makeProduct(1L, "Laptop", 1200.00));
        products.add(makeProduct(2L, "Mouse", 25.50));
        products.add(makeProduct(3L, "Keyboard", 80.00));
        products.add(makeProduct(4L, "Monitor", 300.00));
        products.add(makeProduct(5L, "USB Cable", 15.00));
        products.add(new Product());
    }

    private Product makeProduct(Long id, String name, double price) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        return p;
    }

    public List<Product> getProductsAbovePrice(double price) {
        return products.stream()
                .filter(p -> p.getPrice() > price)
                .toList();
    }

    public List<String> getAllProductNames() {
        return Optional.ofNullable(products)
                .orElse(new ArrayList<>())
                .stream()
                .map(Product::getName)
                .filter(p -> p != null)
                .toList();
    }
}
