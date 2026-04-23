package com.beanannotation.Service;

import com.beanannotation.*;
import com.beanannotation.dto.request.ProductItemDTO;
import com.beanannotation.dto.response.ChatResponseDTO;
import com.beanannotation.dto.response.ProductDTO;
import com.beanannotation.dto.response.ProductResponseDTO;
import com.beanannotation.dto.response.UploadResponseDTO;
import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class ProductClientService {
    @GrpcClient("local-grpc-server")
    private ProductServiceGrpc.ProductServiceBlockingStub synchronousProduct;
    @GrpcClient("local-grpc-server")
    private ProductServiceGrpc.ProductServiceStub asyncStub;

    private Gson gson;

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
    public List<ProductResponseDTO> listProducts(String keyword) throws InterruptedException {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword(keyword == null ? "" : keyword)
                .build();

        List<ProductResponseDTO> result = new ArrayList<>();
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

        CountDownLatch latch = new CountDownLatch(1);
        final int[] result = {0};

        StreamObserver<UploadSummary> responseObserver = new StreamObserver<UploadSummary>() {

            @Override
            public void onNext(UploadSummary summary) {
                System.out.println("Server response count = " + summary.getCount());
                result[0] = summary.getCount();
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Upload failed: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Upload completed");
                latch.countDown();
            }
        };

        StreamObserver<Product> requestObserver = asyncStub.uploadProducts(responseObserver);

        try {
            for (ProductItemDTO dto : products) {

                Product product = Product.newBuilder()
                        .setId(dto.getId()) // ⚠️ bỏ nếu auto generate
                        .setName(dto.getName())
                        .setPrice(dto.getPrice())
                        .build();

                System.out.println("Sending product: " + dto.getName());

                requestObserver.onNext(product);
            }

            requestObserver.onCompleted();

        } catch (Exception e) {
            requestObserver.onError(e);
            throw e;
        }

        // ⚠️ tránh treo vô hạn
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for server response");
        }

        return new UploadResponseDTO(result[0]);
    }
//    public ChatResponseDTO chatProducts(List<Long> ids) throws InterruptedException {
//        CountDownLatch latch = new CountDownLatch(1);
//        List<ChatResponseDTO> responses = new ArrayList<>();
//        Throwable[] error = {null};
//
//        // Mở stream 2 chiều
//        StreamObserver<ProductRequest> requestObserver = asyncStub.chatProducts(
//                new StreamObserver<ProductResponse>() {
//
//                    @Override
//                    public void onNext(ProductResponse response) {
//                        // Nhận response ngay khi server phản hồi từng request
//                        responses.add(convertChatResponse(response));
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        error[0] = t;
//                        latch.countDown();
//                    }
//
//                    @Override
//                    public void onCompleted() {
//                        latch.countDown();
//                    }
//                });
//
//        // Gửi từng id lên — server phản hồi song song
//        for (long id : ids) {
//            requestObserver.onNext(
//                    ProductRequest.newBuilder().setId(id).build()
//            );
//        }
//
//        requestObserver.onCompleted(); // Đóng chiều gửi
//
//        boolean completed = latch.await(10, TimeUnit.SECONDS);
//        if (!completed) throw new RuntimeException("Bidirectional streaming timeout after 10s");
//        if (error[0] != null) throw new RuntimeException("Chat error: " + error[0].getMessage());
//
//        return new ProductDTO.ChatResponse(responses);
//    }
public List<ProductResponseDTO> chatProducts(List<Long> ids) {

    ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9000)
            .usePlaintext()
            .build();

    ProductServiceGrpc.ProductServiceStub stub =
            ProductServiceGrpc.newStub(channel);

    List<ProductResponseDTO> result = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(1);

    StreamObserver<ProductResponse> responseObserver =
            new StreamObserver<ProductResponse>() {
                @Override
                public void onNext(ProductResponse value) {
                    result.add(new ProductResponseDTO(value.getProduct().getId(), value.getProduct().getName(),value.getProduct().getPrice()));
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
    private ProductResponseDTO toDTO(Product p) {
        return new ProductResponseDTO(p.getId(), p.getName(), p.getPrice());
    }
//    public ChatResponseDTO convertChatResponse(List<ProductResponse> grpcResponses) {
//
//        List<ProductItemDTO> products = grpcResponses.stream()
//                .map(res -> {
//                    Product p = res.getProduct();
//
//                    ProductItemDTO dto = new ProductItemDTO();
//                    dto.setId(p.getId());
//                    dto.setName(p.getName());
//                    dto.setPrice(p.getPrice());
//
//                    return dto;
//                })
//                .toList();
//
//        ChatResponseDTO response = new ChatResponseDTO();
//        response.setProducts(products);
//        response.setTotal(products.size());
//
//        return response;
//    }
}
