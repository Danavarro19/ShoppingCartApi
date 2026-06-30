package com.org.productservice.client;

import com.org.productservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "fakeStoreClient", url = "${external.fakestore.base-url}")
public interface FakeStoreClient {
    @GetMapping("/products")
    List<ProductDTO> getAllProducts();

    @GetMapping("/products/{id}")
    ProductDTO getProductById(@PathVariable("id") Long id);

    @GetMapping("/products/categories")
    List<String> getCategories();

    @GetMapping("/products/category/{category}")
    List<ProductDTO> getProductsByCategory(@PathVariable("category") String category);
}