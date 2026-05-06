package com.example.ProductService.service;

import com.example.ProductService.client.MediaServiceClient;
import com.example.ProductService.dto.AttachImagesRequest;
import com.example.ProductService.dto.CreateProductRequest;
import com.example.ProductService.dto.ProductResponse;
import com.example.ProductService.dto.UpdateProductRequest;
import com.example.ProductService.entity.Product;
import com.example.ProductService.event.ProductEvent;
import com.example.ProductService.event.ProductEventProducer;
import com.example.ProductService.exception.ForbiddenException;
import com.example.ProductService.exception.NotFoundException;
import com.example.ProductService.exception.UnauthorizedActionException;
import com.example.ProductService.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final MediaServiceClient mediaServiceClient;
    private final ProductEventProducer eventProducer;

    public ProductResponse create(String sellerId, String roles, CreateProductRequest req) {
        requireSeller(roles);

        Product product = Product.builder()
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .sellerId(sellerId)
                .build();

        ProductResponse response = ProductResponse.from(productRepository.save(product));

        eventProducer.send(new ProductEvent(
                "CREATED", response.id(), sellerId,
                response.name(), response.price(),
                response.imageIds(), LocalDateTime.now()
        ));

        return response;
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse getById(String id) {
        return ProductResponse.from(findOrThrow(id));
    }

    public ProductResponse update(String id, String currentUserId, String roles, UpdateProductRequest req) {
        requireSeller(roles);
        Product product = findOrThrow(id);
        requireOwner(product.getSellerId(), currentUserId);

        if (req.name() != null) product.setName(req.name());
        if (req.description() != null) product.setDescription(req.description());
        if (req.price() != null) product.setPrice(req.price());

        ProductResponse response = ProductResponse.from(productRepository.save(product));

        eventProducer.send(new ProductEvent(
                "UPDATED", response.id(), currentUserId,
                response.name(), response.price(),
                response.imageIds(), LocalDateTime.now()
        ));

        return response;
    }

    public void delete(String id, String currentUserId, String roles) {
        requireSeller(roles);
        Product product = findOrThrow(id);
        requireOwner(product.getSellerId(), currentUserId);
        productRepository.delete(product);

        eventProducer.send(new ProductEvent(
                "DELETED", id, currentUserId,
                product.getName(), product.getPrice(),
                product.getImageIds(), LocalDateTime.now()
        ));
    }

    public ProductResponse attachImages(String id, String currentUserId, String roles, AttachImagesRequest req) {
        requireSeller(roles);
        Product product = findOrThrow(id);
        requireOwner(product.getSellerId(), currentUserId);

        if (!mediaServiceClient.allExist(req.imageIds())) {
            throw new NotFoundException("One or more imageIds do not exist");
        }

        product.getImageIds().addAll(req.imageIds());
        ProductResponse response = ProductResponse.from(productRepository.save(product));

        eventProducer.send(new ProductEvent(
                "UPDATED", response.id(), currentUserId,
                response.name(), response.price(),
                response.imageIds(), LocalDateTime.now()
        ));

        return response;
    }

    private void requireSeller(String roles) {
        if (roles == null || !roles.contains("SELLER")) {
            throw new ForbiddenException("Only sellers can perform this action");
        }
    }

    private void requireOwner(String sellerId, String currentUserId) {
        if (!sellerId.equals(currentUserId)) {
            throw new UnauthorizedActionException("You do not own this resource");
        }
    }

    private Product findOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found: " + id));
    }
}
