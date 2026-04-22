package com.beanannotation.service;

import com.beanannotation.Product;
import com.beanannotation.ProductRequest;
import com.beanannotation.ProductResponse;
import com.beanannotation.ProductServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ProductServerService extends ProductServiceGrpc.ProductServiceImplBase {
    @Override
    public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        long productId = request.getId();
        Product found = productsDB.getProductsFromProductDb().stream()
                .filter(p -> p.getId() == productId)
                .findFirst()
                .orElse(null);
        ProductResponse response =  ProductResponse.newBuilder()
                .setProduct(found != null ? found : Product.newBuilder()
                        .setId(0)
                        .setName("Not found")
                        .setPrice(0)
                        .build())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
