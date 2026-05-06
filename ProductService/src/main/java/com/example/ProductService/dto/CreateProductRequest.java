package com.example.ProductService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateProductRequest(
        @NotBlank String name,
        @NotBlank String description,
        @Positive double price
) {}
