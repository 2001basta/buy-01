package com.example.ProductService.controller;

import com.example.ProductService.dto.AttachImagesRequest;
import com.example.ProductService.dto.CreateProductRequest;
import com.example.ProductService.dto.ProductResponse;
import com.example.ProductService.dto.UpdateProductRequest;
import com.example.ProductService.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @RequestHeader("userId") String userId,
            @RequestHeader("roles") String roles,
            @Valid @RequestBody CreateProductRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(userId, roles, req));
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable String id) {
        return productService.getById(id);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable String id,
            @RequestHeader("userId") String userId,
            @RequestHeader("roles") String roles,
            @RequestBody UpdateProductRequest req) {
        return productService.update(id, userId, roles, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String id,
            @RequestHeader("userId") String userId,
            @RequestHeader("roles") String roles) {
        productService.delete(id, userId, roles);
    }

    @PutMapping("/{id}/images")
    public ProductResponse attachImages(
            @PathVariable String id,
            @RequestHeader("userId") String userId,
            @RequestHeader("roles") String roles,
            @Valid @RequestBody AttachImagesRequest req) {
        return productService.attachImages(id, userId, roles, req);
    }
}
