//package com.beanannotation.integration;
//
//import com.beanannotation.ProductRequest;
//import com.beanannotation.ProductServiceGrpc;
//import com.beanannotation.entity.ProductEntity;
//import com.beanannotation.repositories.ProductRepository;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import io.grpc.StatusRuntimeException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
//
//@SpringBootTest
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//public class ProductGrpcIntegrationTest {
//    @Autowired
//    private ProductRepository productRepository;
//
//    private ManagedChannel channel;
//    private ProductServiceGrpc.ProductServiceBlockingStub stub;
//
//    @BeforeEach
//    void setup() {
//        channel = ManagedChannelBuilder
//                .forAddress("localhost", 9000)
//                .usePlaintext()
//                .build();
//
//        stub = ProductServiceGrpc.newBlockingStub(channel);
//
//        productRepository.deleteAll();
//
//        productRepository.save(
//                ProductEntity.builder()
//                        .id(1L)
//                        .name("Laptop")
//                        .price(1000.0)
//                        .build()
//        );
//    }
//    @AfterEach
//    void tearDown() {
//        channel.shutdownNow();
//    }
//
//    @Test
//    void shouldThrowNotFoundWhenProductMissing() {
//        ProductRequest request = ProductRequest.newBuilder()
//                .setId(999L)
//                .build();
//
//        assertThatThrownBy(() -> stub.getProduct(request))
//                .isInstanceOf(StatusRuntimeException.class)
//                .hasMessageContaining("NOT_FOUND");
//    }
//}
