package com.org.productservice.service;

import com.org.productservice.client.FakeStoreClient;
import com.org.productservice.dto.ProductDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {
    private final FakeStoreClient client;

    public ProductService(FakeStoreClient client) {
        this.client = client;
    }

    @Cacheable("products")
    @Retryable(value = {IOException.class}, maxAttempts = 3)
    public List<ProductDTO> getAllProducts() {
        return client.getAllProducts();
    }

    @Cacheable(value = "product", key = "#id")
    @Retryable(value = {IOException.class}, maxAttempts = 3)
    public ProductDTO getProductById(Long id) {
        try {
            ProductDTO product = client.getProductById(id);
            if (product == null) {
                throw new com.org.productservice.exception.ProductNotFoundException(id);
            }
            return product;
        } catch (feign.FeignException.NotFound e) {
            throw new com.org.productservice.exception.ProductNotFoundException(id);
        }
    }

    @Cacheable("categories")
    @Retryable(value = {IOException.class}, maxAttempts = 3)
    public List<String> getCategories() {
        return client.getCategories();
    }

    @Cacheable(value = "productsByCategory", key = "#category")
    @Retryable(value = {IOException.class}, maxAttempts = 3)
    public List<ProductDTO> getProductsByCategory(String category) {
        try {
            List<ProductDTO> products = client.getProductsByCategory(category);
            if (products == null || products.isEmpty()) {
                throw new com.org.productservice.exception.ProductNotFoundException("No products found in category: " + category);
            }
            return products;
        } catch (feign.FeignException.NotFound e) {
            throw new com.org.productservice.exception.ProductNotFoundException("Category not found: " + category);
        }
    }
}
