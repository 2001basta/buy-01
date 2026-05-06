package com.example.ProductService.dto;

public record UpdateProductRequest(
        String name,
        String description,
        Double price
) {}
