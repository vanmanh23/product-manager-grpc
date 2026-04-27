package com.beanannotation.integration;

import com.beanannotation.integration.dto.request.ProductItemDTO;
import com.beanannotation.integration.dto.request.UploadRequestDTO;
import com.beanannotation.integration.dto.response.ProductDTO;
import com.beanannotation.integration.dto.response.UploadResponseDTO;
import com.beanannotation.integration.repository.ProductEntityFakeRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Testcontainers
public class ProductIntegrationTest {
    private String baseUrl;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ProductEntityFakeRepository productRepositoryTest;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8081/api/product";
        productRepositoryTest.deleteAll();
    }
    @AfterEach
    void cleanup() {
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

    @Test
    void shouldGetProductById() {
        // Given
        UploadRequestDTO request = new UploadRequestDTO();
        List<ProductItemDTO> productDTOS = Arrays.asList(
                new ProductItemDTO(1L, "Iphone 3", 1000.0)
        );
        request.setProducts(productDTOS);
        // When
        ResponseEntity<UploadResponseDTO> createResponse = restTemplate.postForEntity(
                baseUrl + "/upload", request, UploadResponseDTO.class
        );
        // Given
        String idProduct = "1";
        // When
        ResponseEntity<ProductDTO> getResponse = restTemplate.getForEntity(
                baseUrl + "/{id}", ProductDTO.class, idProduct
        );
        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Iphone 3");
    }

    @Test
    void shouldReturnListProducts() {
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
        // Given
        String searchCharacter = "iphone";
        // When
        ResponseEntity<List<ProductDTO>> getResponse = restTemplate.exchange(
                baseUrl + "?keyword={keyword}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                },
                searchCharacter
        );
        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get(0).getName()).isEqualTo("Iphone 3");
    }

    @Test
    void shouldReturnProductBidirectionalStreaming() {
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
        // Given
        HttpEntity<List<Long>> productIds =
                new HttpEntity<>(Arrays.asList(1L, 2L));
        // When
        ResponseEntity<List<ProductDTO>> response = restTemplate.exchange(
                baseUrl + "/bidirectional",
                HttpMethod.POST,
                productIds,
                new ParameterizedTypeReference<List<ProductDTO>>() {
                }
        );
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }
}
