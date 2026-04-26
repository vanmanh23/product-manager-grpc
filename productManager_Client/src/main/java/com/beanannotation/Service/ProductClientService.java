package com.beanannotation.Service;

import com.beanannotation.*;
import com.beanannotation.dto.request.ProductItemDTO;
import com.beanannotation.dto.response.ProductDTO;
import com.beanannotation.dto.response.UploadResponseDTO;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Log4j2
public class ProductClientService {
    @GrpcClient("local-grpc-server")
    private ProductServiceGrpc.ProductServiceBlockingStub synchronousProduct;
    @GrpcClient("local-grpc-server")
    private ProductServiceGrpc.ProductServiceStub asyncStub;
    public ProductDTO getProductById(int productId) {
        ProductRequest productRequest = ProductRequest.newBuilder()
                .setId(productId)
                .build();

        ProductResponse productResponse = synchronousProduct.getProduct(productRequest);
        Product product = productResponse.getProduct();

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());

        return dto;
    }

    public List<ProductDTO> listProducts(String keyword) throws InterruptedException {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword(keyword == null ? "" : keyword)
                .build();

        List<ProductDTO> result = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        asyncStub.listProducts(request, new StreamObserver<ProductListResponse>() {

            @Override
            public void onNext(ProductListResponse response) {
                result.add(toDTO(response.getProduct()));
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
                throw new RuntimeException("Server streaming error: " + t.getMessage(), t);
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        if (!completed) {
            throw new RuntimeException("Server streaming timeout after 10s");
        }
        return result;
    }

    public UploadResponseDTO uploadProducts(List<ProductItemDTO> products) throws InterruptedException {

        AtomicInteger result = new AtomicInteger();
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<Product> requestObserver = asyncStub.withDeadlineAfter(5, TimeUnit.SECONDS)
                .uploadProducts(new StreamObserver<UploadSummary>() {
                    @Override
                    public void onNext(UploadSummary value) {
                        result.set(value.getCount());
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Error in client streaming call", t);
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Client streaming completed");
                    }
                });
        // Send multiple messages to the server
        for (ProductItemDTO dto : products) {
            Product product = Product.newBuilder()
                    .setId(dto.getId())
                    .setName(dto.getName())
                    .setPrice(dto.getPrice())
                    .build();
            requestObserver.onNext(product);
        }
        requestObserver.onCompleted();
        latch.await(2, TimeUnit.SECONDS);
        return new UploadResponseDTO(result.get());
    }

    public List<ProductDTO> bidirectionalStreamingProducts(List<Long> ids) {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9000)
                .usePlaintext()
                .build();
        ProductServiceGrpc.ProductServiceStub stub =
                ProductServiceGrpc.newStub(channel);
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
                stub.chatProducts(responseObserver);

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
        return result;
    }
    private ProductDTO toDTO(Product p) {
        return new ProductDTO(p.getId(), p.getName(), p.getPrice());
    }
}
