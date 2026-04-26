//package com.beanannotation.integration;
//
//import com.beanannotation.ProductRequest;
//import com.beanannotation.ProductResponse;
//import com.beanannotation.ProductServiceGrpc;
//import io.grpc.ManagedChannel;
//import io.grpc.ManagedChannelBuilder;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.web.server.LocalServerPort;
//import org.springframework.web.client.RestTemplate;
//import com.beanannotation.repository.ProductEntityFakeRepository;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class ProductGrpcIntegrationTest {
//
//    @LocalServerPort
//    private int port;
//    private String baseUrl = "http://localhost";
//
//    private static RestTemplate restTemplate;
//
//    @Autowired
//    private ProductEntityFakeRepository productEntityFakeRepository;
//
//    @BeforeAll
//    public static void init() {
//        restTemplate = new RestTemplate();
//    }
//
//    @BeforeEach
//    public void setUp() {
//        baseUrl = baseUrl.concat(":").concat(port + "").concat("/products");
//    }
//
//    private ManagedChannel channel;
//    private ProductServiceGrpc.ProductServiceBlockingStub stub;
//
//    @BeforeAll
//    void setup() {
//        channel = ManagedChannelBuilder
//                .forAddress("localhost", 9000)
//                .usePlaintext()
//                .build();
//
//        stub = ProductServiceGrpc.newBlockingStub(channel);
//    }
//
//    @AfterAll
//    void teardown() {
//        channel.shutdown();
//    }
//
//    @Test
//    void testGetProduct_success() {
//        // arrange
//        ProductRequest request = ProductRequest.newBuilder()
//                .setId(1)
//                .build();
//
//        // act
//        ProductResponse response = stub.getProduct(request);
//
//        // assert
//        assertEquals(1, response.getProduct().getId());
//    }
//}