package com.beanannotation.service;

import com.beanannotation.Product;
import com.beanannotation.ProductRequest;
import com.beanannotation.ProductResponse;
import com.beanannotation.ProductServiceGrpc;
import com.beanannotation.entity.ProductEntity;
import com.beanannotation.exceptions.ProductNotFoundException;
import com.beanannotation.repositories.ProductRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
public class ProductServerService extends ProductServiceGrpc.ProductServiceImplBase {
    @Autowired
    private ProductRepository productRepository;
    @Override
    public void getProduct(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        long productId = request.getId();

        ProductEntity found = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("product not found"));
        Product product = Product.newBuilder()
                .setId(found.getId())
                .setName(found.getName())
                .setPrice(found.getPrice())
                .build();
        ProductResponse response =  ProductResponse.newBuilder()
                .setProduct(product)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
