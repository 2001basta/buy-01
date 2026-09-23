package com.example.ProductService.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
        @Size(max = 120) @Pattern(regexp = "(?s).*\\S.*") String name,
        @Size(max = 2000) @Pattern(regexp = "(?s).*\\S.*") String description,
        @Positive Double price,
        String[] imageIds
) {}
