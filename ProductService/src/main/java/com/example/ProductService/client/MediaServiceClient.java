package com.example.ProductService.client;

import com.example.ProductService.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Component
public class MediaServiceClient {

    private final RestTemplate restTemplate;
    private final String mediaServiceUrl;

    public MediaServiceClient(RestTemplate restTemplate,
                              @Value("${media.service.url}") String mediaServiceUrl) {
        this.restTemplate = restTemplate;
        this.mediaServiceUrl = mediaServiceUrl;
    }

    @CircuitBreaker(name = "mediaService", fallbackMethod = "allExistFallback")
    @SuppressWarnings("unchecked")
    public boolean allExist(List<String> imageIds) {
        String url = UriComponentsBuilder
                .fromUriString(mediaServiceUrl + "/api/media/exists")
                .queryParam("ids", imageIds)
                .toUriString();

        Map<String, Boolean> response = restTemplate.getForObject(url, Map.class);
        return response != null && Boolean.TRUE.equals(response.get("allExist"));
    }

    public boolean allExistFallback(List<String> imageIds, Exception ex) {
        throw new ServiceUnavailableException("Media service is unavailable — cannot validate images");
    }
}
