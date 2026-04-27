package com.beanannotation.unitTest.service;

import com.beanannotation.*;
import com.beanannotation.entity.ProductEntity;
import com.beanannotation.exceptions.ProductNotFoundException;
import com.beanannotation.repositories.ProductRepository;
import com.beanannotation.service.ProductServerService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProductServerServiceUnitTest {
    // Mock dependency
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StreamObserver<ProductResponse> responseObserver;
    @Mock
    private StreamObserver<ProductListResponse> responseObserverListProduct;
    @InjectMocks
    private ProductServerService productServerService;
    private ProductEntity mockProductEntity;
    private ProductEntity mockProductEntityB;

    @BeforeEach
    public void setUp() {
        mockProductEntity = new ProductEntity(1L, "Laptop", 1000.0);
        mockProductEntityB = new ProductEntity();
        mockProductEntityB.setId(2L);
        mockProductEntityB.setName("iPhone 15");
        mockProductEntityB.setPrice(30000000.0);
    }

    //getProduct
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
        assertEquals(1, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals(1000, result.getPrice());
    }

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

    @Test
    void getProduct_idZero_throwsProductNotFoundException() {
        ProductRequest request = ProductRequest.newBuilder()
                .setId(0L)
                .build();

        when(productRepository.findById(0L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productServerService.getProduct(request, responseObserver));

        verify(productRepository).findById(0L);
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void getProduct_negativeId_throwsProductNotFoundException() {
        ProductRequest request = ProductRequest.newBuilder()
                .setId(-5L)
                .build();

        when(productRepository.findById(-5L))
                .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productServerService.getProduct(request, responseObserver));

        verify(productRepository).findById(-5L);
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void getProduct_repositoryThrowsException_propagatesException() {
        ProductRequest request = ProductRequest.newBuilder()
                .setId(1L)
                .build();

        when(productRepository.findById(1L))
                .thenThrow(new RuntimeException("DB connection failed"));

        assertThrows(RuntimeException.class,
                () -> productServerService.getProduct(request, responseObserver));

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    //  listProduct
    @Test
    void listProducts_keywordMatches_sendsAllProducts() {
        // given
        ProductEntity entity1 = new ProductEntity(1L, "Laptop", 1000.0);
        ProductEntity entity2 = new ProductEntity(2L, "Samsung S24", 899.99);

        when(productRepository.findByNameContainingIgnoreCase("laptop"))
                .thenReturn(new ArrayList<>(Arrays.asList(entity1, entity2)));

        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("laptop")
                .build();

        // when
        productServerService.listProducts(request, responseObserverListProduct);

        // then
        verify(responseObserverListProduct, times(2)).onNext(any(ProductListResponse.class));
        verify(responseObserverListProduct).onCompleted();
    }

    @Test
    void listProducts_keywordNotMatches_sendsNoProducts() {
        // given
        when(productRepository.findByNameContainingIgnoreCase("abc"))
                .thenReturn(Collections.emptyList());

        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("abc")
                .build();

        // when
        productServerService.listProducts(request, responseObserverListProduct);

        // then
        verify(responseObserverListProduct, never()).onNext(any());
        verify(responseObserverListProduct).onCompleted();
    }

    @Test
    void listProducts_emptyKeyword_sendsAllProducts() {
        ProductEntity entity2 = new ProductEntity();
        entity2.setId(1L);
        entity2.setName("Laptop");
        entity2.setPrice(1000.0);

        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("")
                .build();

        when(productRepository.findByNameContainingIgnoreCase(""))
                .thenReturn(new ArrayList<>(Arrays.asList(mockProductEntity, entity2)));

        productServerService.listProducts(request, responseObserverListProduct);

        verify(productRepository).findByNameContainingIgnoreCase("");
        verify(responseObserverListProduct, times(2)).onNext(any(ProductListResponse.class));
        verify(responseObserverListProduct, times(1)).onCompleted();
    }

    @Test
    void listProducts_caseInsensitiveKeyword_findsProduct() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("LAPTOP")
                .build();

        when(productRepository.findByNameContainingIgnoreCase("LAPTOP"))
                .thenReturn(new ArrayList<>(Arrays.asList(mockProductEntity)));

        productServerService.listProducts(request, responseObserverListProduct);

        verify(productRepository).findByNameContainingIgnoreCase("LAPTOP");
        verify(responseObserverListProduct, times(1)).onNext(any(ProductListResponse.class));
        verify(responseObserverListProduct, times(1)).onCompleted();
    }

    //ChatProducts
    private ProductRequest req(long id) {
        return ProductRequest.newBuilder().setId(id).build();
    }

    // gửi 1 request qua stream và complete
    private void sendAndComplete(StreamObserver<ProductRequest> clientStream,
                                 ProductRequest... requests) {
        for (ProductRequest r : requests) clientStream.onNext(r);
        clientStream.onCompleted();
    }

    @Test
    void chatProducts_oneRequest_oneResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProductEntity));

        StreamObserver<ProductRequest> clientStream =
                productServerService.chatProducts(responseObserver);

        sendAndComplete(clientStream, req(1L));

        verify(responseObserver, times(1)).onNext(any(ProductResponse.class));
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }
    @Test
    void chatProducts_multipleRequests_multipleResponsesInOrder() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProductEntity));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mockProductEntityB));

        StreamObserver<ProductRequest> clientStream =
                productServerService.chatProducts(responseObserver);

        sendAndComplete(clientStream, req(1L), req(2L));

        ArgumentCaptor<ProductResponse> captor =
                ArgumentCaptor.forClass(ProductResponse.class);
        verify(responseObserver, times(2)).onNext(captor.capture());

        List<ProductResponse> responses = captor.getAllValues();
        assertEquals(1L, responses.get(0).getProduct().getId());
        assertEquals(2L, responses.get(1).getProduct().getId());

        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
    }

    @Test
    void chatProducts_productNotFound_callsOnError() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        StreamObserver<ProductRequest> clientStream =
                productServerService.chatProducts(responseObserver);

        clientStream.onNext(req(99L));   // không complete — sau onError thường dừng

        verify(responseObserver, times(1)).onError(any(RuntimeException.class));
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }

    @Test
    void chatProducts_noRequestSent_onlyCompletedIsCalled() {
        StreamObserver<ProductRequest> clientStream =
                productServerService.chatProducts(responseObserver);

        clientStream.onCompleted();     // client kết thúc ngay, không gửi gì

        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, times(1)).onCompleted();
        verify(responseObserver, never()).onError(any());
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void chatProducts_multipleRequests_responseOrderMatchesRequestOrder() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(mockProductEntity));
        when(productRepository.findById(2L)).thenReturn(Optional.of(mockProductEntityB));

        StreamObserver<ProductRequest> clientStream =
                productServerService.chatProducts(responseObserver);

        sendAndComplete(clientStream, req(1L), req(2L), req(1L));   // A→B→A

        ArgumentCaptor<ProductResponse> captor =
                ArgumentCaptor.forClass(ProductResponse.class);
        verify(responseObserver, times(3)).onNext(captor.capture());

        List<ProductResponse> responses = captor.getAllValues();
        assertAll("response order A→B→A",
                () -> assertEquals(1L, responses.get(0).getProduct().getId()),
                () -> assertEquals(2L, responses.get(1).getProduct().getId()),
                () -> assertEquals(1L, responses.get(2).getProduct().getId())
        );

        verify(responseObserver, times(1)).onCompleted();
    }
}