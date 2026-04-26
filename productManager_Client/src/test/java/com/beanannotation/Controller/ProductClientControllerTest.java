package com.beanannotation.Controller;

import com.beanannotation.Service.ProductClientService;
import com.beanannotation.dto.request.ProductItemDTO;
import com.beanannotation.dto.request.UploadRequestDTO;
import com.beanannotation.dto.response.ProductDTO;
import com.beanannotation.dto.response.UploadResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductClientController.class)
class ProductClientControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProductClientService productClientService;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    }

    @Test
    void uploadProducts() throws Exception {
        // Given
        UploadRequestDTO request = new UploadRequestDTO();
        List<ProductItemDTO> productDTOS = Arrays.asList(
                new ProductItemDTO(1L, "Laptop HP", 1000.0),
                new ProductItemDTO(2L, "Laptop DELL", 1030.0)
        );
        request.setProducts(productDTOS);

        UploadResponseDTO response = new UploadResponseDTO(2);
        when(productClientService.uploadProducts(anyList()))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/product/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));

        verify(productClientService).uploadProducts(anyList());
    }

    @Test
    void getProduct() throws Exception {
        // Given
        int productId = 1;
        ProductDTO response = new ProductDTO(1L, "Laptop HP", 1000.0);
        when(productClientService.getProductById(productId))
                .thenReturn(response);
        // When & Then
        mockMvc.perform(get("/api/product/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop HP"))
                .andExpect(jsonPath("$.price").value(1000.0));

        verify(productClientService).getProductById(productId);
    }

    @Test
    void listProducts() throws Exception {
        // Given
        String searchParam = "lap";
        List<ProductDTO> response = Arrays.asList(
                new ProductDTO(1L, "Laptop HP", 1000.0)
        );
        when(productClientService.listProducts(searchParam))
                .thenReturn(response);
        // When & Then
        mockMvc.perform(get("/api/product").param("keyword", searchParam))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop HP"));

        verify(productClientService).listProducts(searchParam);
    }

    @Test
    void productBidirectionalStreaming() throws Exception {
        // Given
        List<Long> productIds = Arrays.asList(1L, 2L);
        List<ProductDTO> response = Arrays.asList(
                new ProductDTO(1L, "Laptop HP", 1000.0),
                new ProductDTO(2L, "Laptop DELL", 1030.0)
        );
        when(productClientService.bidirectionalStreamingProducts(productIds))
                .thenReturn(response);
        // When & Then
        mockMvc.perform(post("/api/product/bidirectional")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Laptop HP"))
                .andExpect(jsonPath("$[1].name").value("Laptop DELL"));

        verify(productClientService).bidirectionalStreamingProducts(productIds);
    }
    @Test
    void shouldReturnBadRequestWhenValidationFails() throws Exception {
        // Given
        UploadRequestDTO request = new UploadRequestDTO();
        List<ProductItemDTO> productDTOS = Arrays.asList(
                new ProductItemDTO(3L, " ", 1000.0),
                new ProductItemDTO(4L, "test", 1030.0)
        );
        request.setProducts(productDTOS);
        // When & Then
        mockMvc.perform(post("/api/product/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(productClientService, never()).uploadProducts(anyList());
    }
}