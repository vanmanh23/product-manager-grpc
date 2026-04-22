package controller;

import com.beanannotation.ProductRequest;
import com.beanannotation.ProductResponse;
import com.beanannotation.ProductServiceGrpc;
import com.beanannotation.Service.ProductClientService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = ProductClientService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductGrpcIntegrationTest {

    private ManagedChannel channel;
    private ProductServiceGrpc.ProductServiceBlockingStub stub;

    @BeforeAll
    void setup() {
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9000)
                .usePlaintext()
                .build();

        stub = ProductServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    void teardown() {
        channel.shutdown();
    }

    @Test
    void testGetProduct_success() {
        // arrange
        ProductRequest request = ProductRequest.newBuilder()
                .setId(1)
                .build();

        // act
        ProductResponse response = stub.getProduct(request);

        // assert
        assertEquals(1, response.getProduct().getId());
    }
}