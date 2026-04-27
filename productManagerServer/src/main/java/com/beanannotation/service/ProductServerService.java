package com.beanannotation.service;

import com.beanannotation.*;
import com.beanannotation.entity.ProductEntity;
import com.beanannotation.exceptions.ProductNotFoundException;
import com.beanannotation.repositories.ProductRepository;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
@Log4j2
public class ProductServerService extends ProductServiceGrpc.ProductServiceImplBase {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            long productId = request.getId();

            ProductEntity found = productRepository.findById(productId)
                    .orElseThrow(() -> Status.NOT_FOUND
                            .withDescription("Product not found")
                            .asRuntimeException()
                    );
            Product product = Product.newBuilder()
                    .setId(found.getId())
                    .setName(found.getName())
                    .setPrice(found.getPrice())
                    .build();
            ProductResponse response = ProductResponse.newBuilder()
                    .setProduct(product)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException ex) {
            responseObserver.onError(ex);
        } catch (Exception ex) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription(ex.getMessage())
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void listProducts(ProductListRequest request, StreamObserver<ProductListResponse> responseObserver) {
        String keyword = request.getKeyword();
        List<ProductEntity> products = productRepository.findByNameContainingIgnoreCase(keyword);
        for (ProductEntity productEntiry : products) {
            Product product = Product.newBuilder()
                    .setId(productEntiry.getId())
                    .setName(productEntiry.getName())
                    .setPrice(productEntiry.getPrice())
                    .build();
            ProductListResponse response = ProductListResponse.newBuilder()
                    .setProduct(product)
                    .build();
            responseObserver.onNext(response);
        }

        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<Product> uploadProducts(StreamObserver<UploadSummary> responseObserver) {
        return new StreamObserver<Product>() {
            private int count = 0;

            @Override
            public void onNext(Product product) {
                ProductEntity entity = new ProductEntity();
                entity.setId(Long.valueOf(product.getId()));
                entity.setName(product.getName());
                entity.setPrice(product.getPrice());
                productRepository.save(entity);

                count++;
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("uploadProducts client-streaming RPC failed");
            }

            @Override
            public void onCompleted() {
                UploadSummary summary = UploadSummary.newBuilder()
                        .setCount(count).build();
                responseObserver.onNext(summary);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<ProductRequest> chatProducts(StreamObserver<ProductResponse> responseObserver) {
        return new StreamObserver<ProductRequest>() {
            @Override
            public void onNext(ProductRequest request) {
                long productId = request.getId();
                try {
                    ProductEntity entity = productRepository.findById(productId)
                            .orElseThrow(() -> new RuntimeException("Product not found"));

                    Product product = Product.newBuilder()
                            .setId(entity.getId())
                            .setName(entity.getName())
                            .setPrice(entity.getPrice())
                            .build();

                    ProductResponse response = ProductResponse.newBuilder()
                            .setProduct(product)
                            .build();
                    // gửi lại cho client
                    responseObserver.onNext(response);

                } catch (Exception e) {
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Client error: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Client completed sending requests");
                responseObserver.onCompleted();
            }
        };
    }
}
