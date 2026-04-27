package com.beanannotation.integration;

import com.beanannotation.integration.dto.request.ProductItemDTO;
import com.beanannotation.integration.dto.request.UploadRequestDTO;
import com.beanannotation.integration.dto.response.ProductDTO;
import com.beanannotation.integration.dto.response.UploadResponseDTO;
import com.beanannotation.integration.repository.ProductEntityFakeRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class ProductIntegrationTest {
@LocalServerPort
private int port;
    private String baseUrl;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ProductEntityFakeRepository productRepositoryTest;
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8081/api/product";
        productRepositoryTest.deleteAll();
    }
    @Test
    void shouldUploadProducts() {
        // Given
        UploadRequestDTO request = new UploadRequestDTO();
        List<ProductItemDTO> productDTOS = Arrays.asList(
                new ProductItemDTO(1L, "Iphone 3", 1000.0),
                new ProductItemDTO(2L, "Iphone 4", 1030.0)
        );
        request.setProducts(productDTOS);
        // When
        ResponseEntity<UploadResponseDTO> createResponse = restTemplate.postForEntity(
                baseUrl + "/upload", request, UploadResponseDTO.class
        );
        // Then
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody().getCount()).isEqualTo(2);
        // When - Get user
        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/{id}", ProductDTO.class, 1L
        );
        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Iphone 3");
    }
}
