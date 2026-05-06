package com.example.ProductService.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AttachImagesRequest(
        @NotEmpty List<String> imageIds
) {}
