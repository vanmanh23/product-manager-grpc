package com.beanannotation.service;

import com.beanannotation.*;
import com.beanannotation.entity.ProductEntity;
import com.beanannotation.exceptions.ProductNotFoundException;
import com.beanannotation.repositories.ProductRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServerServiceTest {
    // Mock dependency
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StreamObserver<ProductResponse> responseObserver;
    @InjectMocks
    private ProductServerService productServerService;
    private ProductEntity mockProductEntity;
    @BeforeEach
    public void setUp() {
        mockProductEntity = new ProductEntity(1L, "Laptop", 1000);
    }

    // Case 1: Tìm thấy product
    @Test
    public void getProduct_WhenProductExists_ShouldReturnProduct() {
        // Arrange
        long productId = 1;
        when(productRepository.findById(productId))
                .thenReturn(Optional.of(mockProductEntity));
        ProductRequest request = ProductRequest.newBuilder().setId(1).build();
        ArgumentCaptor<ProductResponse> captor = ArgumentCaptor.forClass(ProductResponse.class);
        // Act
        productServerService.getProduct(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        Product result = captor.getValue().getProduct();
        assertEquals(1,        result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(1000,     result.getPrice());
    }
    // Case 2: không Tìm thấy product
    @Test
    public void getProduct_WhenProductNotExists_ShouldReturnError() {
        // Given
        long productIdNotExists = 4L;

        Mockito.when(productRepository.findById(productIdNotExists))
                .thenReturn(Optional.empty());

        ProductRequest request = ProductRequest.newBuilder()
                .setId(productIdNotExists)
                .build();

        // When + Then
        assertThrows(ProductNotFoundException.class, () ->
                productServerService.getProduct(request, responseObserver)
        );
        Mockito.verify(responseObserver, Mockito.never()).onNext(Mockito.any());
        Mockito.verify(responseObserver, Mockito.never()).onCompleted();
    }
}