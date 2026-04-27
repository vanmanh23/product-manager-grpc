package com.beanannotation.integration;

import com.beanannotation.*;
import com.beanannotation.integration.dto.response.ProductDTO;
import com.beanannotation.integration.entity.ProductEntityFake;
import com.beanannotation.integration.repository.ProductEntityFakeRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
@Log4j2
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductGrpcIntegrationTest {
    @Autowired
    private ProductEntityFakeRepository productEntityFakeRepository;
    private ManagedChannel channel;
    private ProductServiceGrpc.ProductServiceBlockingStub blockingStub;
    private ProductServiceGrpc.ProductServiceStub asyncStub;

    @BeforeEach
    void setup() {
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9000)
                .usePlaintext()
                .build();

        blockingStub = ProductServiceGrpc.newBlockingStub(channel);
        asyncStub = ProductServiceGrpc.newStub(channel);

        productEntityFakeRepository.deleteAll();

        productEntityFakeRepository.save(
                ProductEntityFake.builder()
                        .id(1L)
                        .name("Laptop")
                        .price(1000.0)
                        .build()
        );
    }
    @AfterEach
    void tearDown() {
        channel.shutdownNow();
    }

    @Test
    void shouldThrowNotFoundWhenProductMissing() {
        ProductRequest request = ProductRequest.newBuilder()
                .setId(999L)
                .build();

        assertThatThrownBy(() -> blockingStub.getProduct(request))
                .isInstanceOf(StatusRuntimeException.class)
                .hasMessageContaining("Product not found");
    }
    @Test
    void shouldReturnProductSuccessfully() {
        productEntityFakeRepository.save(
                ProductEntityFake.builder()
                        .id(1L)
                        .name("Laptop")
                        .price(1000.0)
                        .build()
        );
        ProductResponse response = blockingStub.getProduct(
                ProductRequest.newBuilder()
                        .setId(1L)
                        .build()
        );

        assertThat(response.getProduct().getId()).isEqualTo(1L);
        assertThat(response.getProduct().getName()).isEqualTo("Laptop");
        assertThat(response.getProduct().getPrice()).isEqualTo(1000.0);
    }
    @Test
    void shouldUploadProductSuccessfully() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UploadSummary> summaryRef = new AtomicReference<>();

        StreamObserver<Product> requestObserver =
                asyncStub.uploadProducts(new StreamObserver<UploadSummary>() {
                    @Override
                    public void onNext(UploadSummary value) {
                        summaryRef.set(value);
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                        log.info("Upload failed: " + t.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                });

        requestObserver.onNext(
                Product.newBuilder()
                        .setId(1L)
                        .setName("Laptop")
                        .setPrice(1000.0)
                        .build()
        );

        requestObserver.onNext(
                Product.newBuilder()
                        .setId(2L)
                        .setName("Keyboard")
                        .setPrice(50.0)
                        .build()
        );

        requestObserver.onCompleted();

        latch.await(5, TimeUnit.SECONDS);

        assertThat(summaryRef.get()).isNotNull();
        assertThat(summaryRef.get().getCount()).isEqualTo(2);

    }
    @Test
    void shouldFilterProductsByKeyword() {
        productEntityFakeRepository.save(
                ProductEntityFake.builder().name("Gaming Laptop").price(1000.0).build()
        );
        productEntityFakeRepository.save(
                ProductEntityFake.builder().name("Mechanical Keyboard").price(100.0).build()
        );

        Iterator<ProductListResponse> responses =
                blockingStub.listProducts(
                        ProductListRequest.newBuilder()
                                .setKeyword("Laptop")
                                .build()
                );
        List<ProductListResponse> result = new ArrayList<>();
        responses.forEachRemaining(result::add);

        assertThat(result.get(0).getProduct().getName())
                .contains("Laptop");
    }
    @Test
    void bidirectionalStreamingProducts() throws Exception{
        List<Long> ids = Arrays.asList(1L, 2L);
        List<ProductDTO> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ProductResponse> responseObserver =
                new StreamObserver<ProductResponse>() {
                    @Override
                    public void onNext(ProductResponse value) {
                        result.add(new ProductDTO(value.getProduct().getId(), value.getProduct().getName(), value.getProduct().getPrice()));
                    }

                    @Override
                    public void onError(Throwable t) {
                        latch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        latch.countDown();
                    }
                };

        StreamObserver<ProductRequest> requestObserver =
                asyncStub.chatProducts(responseObserver);

        // gửi nhiều request
        for (Long id : ids) {
            requestObserver.onNext(
                    ProductRequest.newBuilder().setId(id).build()
            );
        }
        requestObserver.onCompleted();
        try {
            latch.await(); // đợi server trả hết
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        assertThat(result).hasSize(2);
    }
}
