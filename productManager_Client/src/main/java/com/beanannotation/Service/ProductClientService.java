package com.beanannotation.Service;

import com.beanannotation.*;
import com.beanannotation.dto.response.ProductDto;
import com.google.protobuf.Descriptors;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductClientService {
    @GrpcClient("local-grpc-server")
    private ProductServiceGrpc.ProductServiceBlockingStub synchronousProduct;
//    public Map<Descriptors.FieldDescriptor, Object> getProductById(int productId) {
//        ProductRequest productRequest = ProductRequest.newBuilder().setId(productId).build();
//        ProductResponse productResponse = synchronousProduct.getProduct(productRequest);
//
//        return productResponse.getAllFields();
//    }
public ProductDto getProductById(int productId) {
    ProductRequest productRequest = ProductRequest.newBuilder()
            .setId(productId)
            .build();

    ProductResponse productResponse = synchronousProduct.getProduct(productRequest);
    Product product = productResponse.getProduct();

    ProductDto dto = new ProductDto();
    dto.setId(product.getId());
    dto.setName(product.getName());
    dto.setPrice(product.getPrice());

    return dto;
}
}
