package com.beanannotation.service.IntegrationTest;


import com.beanannotation.*;
import com.beanannotation.entity.ProductEntity;
import com.beanannotation.repositories.ProductRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = ServerApplication.class
)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ListProductsIntegrationTest {
    private static ManagedChannel channel;
    private static ProductServiceGrpc.ProductServiceBlockingStub blockingStub;

    @Autowired
    private ProductRepository productRepository;

    @BeforeAll
    static void startChannel() {
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9000)
                .usePlaintext()
                .build();
        blockingStub = ProductServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void shutdownChannel() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @BeforeEach
    void seedDatabase() {
        productRepository.deleteAll();

        ProductEntity laptop = new ProductEntity();
        laptop.setName("laptop");
        laptop.setPrice(1000.0);

        ProductEntity phone = new ProductEntity();
        phone.setName("Samsung S24");
        phone.setPrice(899.99);

        ProductEntity tablet = new ProductEntity();
        tablet.setName("Samsung Tablet");
        tablet.setPrice(15000000.0);

        productRepository.saveAll(new ArrayList<>(Arrays.asList(laptop, phone, tablet)));
    }

    @AfterEach
    void cleanDatabase() {
        productRepository.deleteAll();
    }


    // ─── Helper: collect toàn bộ response từ iterator
    private List<ProductListResponse> collectAll(
            java.util.Iterator<ProductListResponse> iterator) {
        List<ProductListResponse> results = new ArrayList<>();
        iterator.forEachRemaining(results::add);
        return results;
    }


    @Test
    @Order(1)
    void listProducts_keywordMatchesOneProduct_returnsOneResponse() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("laptop")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertEquals(1, responses.size());
        assertEquals("laptop", responses.get(0).getProduct().getName());
    }

    @Test
    @Order(2)
    void listProducts_keywordMatchesMultiple_returnsAllMatched() {
        // "a" xuất hiện trong cả "Laptop Dell", "Samsung Tablet" → ≥ 2
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("a")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertTrue(responses.size() >= 2,
                "Phải có ít nhất 2 sản phẩm chứa chữ 'a'");
    }

    @Test
    @Order(3)
    void listProducts_emptyKeyword_returnsAllProducts() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertEquals(3, responses.size());
    }

    @Test
    @Order(4)
    void listProducts_keywordNotFound_returnsEmptyStream() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("xyz_not_exist_999")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertTrue(responses.isEmpty(), "Không có sản phẩm nào khớp phải trả stream rỗng");
    }


    // IT5 — Case-insensitive: "laptop" (thường) khớp "Laptop Dell" (hoa chữ đầu)
    @Test
    @Order(5)
    void listProducts_caseInsensitiveKeyword_findsProduct() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("laptop")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertEquals(1, responses.size());
        assertTrue(
                responses.get(0).getProduct().getName()
                        .equalsIgnoreCase("Laptop Dell"),
                "Tên sản phẩm phải khớp không phân biệt hoa thường"
        );
    }


    @Test
    @Order(6)
    void listProducts_validKeyword_responseContainsAllFields() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("iPhone")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertEquals(1, responses.size());
        Product product = responses.get(0).getProduct();

        assertAll("product fields",
                () -> assertTrue(product.getId() > 0,         "id phải > 0"),
                () -> assertEquals("iPhone 15", product.getName()),
                () -> assertEquals(30000000.0,  product.getPrice(), 0.001)
        );
    }

    @Test
    @Order(7)
    void listProducts_emptyDatabase_returnsEmptyStream() {
        productRepository.deleteAll();     // xóa hết data

        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        assertTrue(responses.isEmpty(), "DB trống phải trả stream rỗng");
    }

    @Test
    @Order(8)
    void listProducts_emptyKeyword_responseOrderMatchesInsertOrder() {
        ProductListRequest request = ProductListRequest.newBuilder()
                .setKeyword("")
                .build();

        List<ProductListResponse> responses =
                collectAll(blockingStub.listProducts(request));

        List<String> names = responses.stream()
                .map(r -> r.getProduct().getName())
                .collect(Collectors.toList());

        assertEquals(new ArrayList<>(Arrays.asList("laptop", "Samsung S24")), names);
    }

    @Test
    @Order(9)
    void listProducts_calledMultipleTimes_eachCallReturnsCorrectResult() {
        ProductListRequest reqDell   = ProductListRequest.newBuilder().setKeyword("Dell").build();
        ProductListRequest reqiPhone = ProductListRequest.newBuilder().setKeyword("iPhone").build();

        List<ProductListResponse> firstCall  = collectAll(blockingStub.listProducts(reqDell));
        List<ProductListResponse> secondCall = collectAll(blockingStub.listProducts(reqiPhone));

        assertEquals(1, firstCall.size());
        assertEquals("Laptop Dell", firstCall.get(0).getProduct().getName());

        assertEquals(1, secondCall.size());
        assertEquals("iPhone 15", secondCall.get(0).getProduct().getName());
    }

    @Test
    @Order(10)
    void listProducts_specialCharKeyword_doesNotThrowException() {
        List<String> specialKeywords = new ArrayList<>(Arrays.asList("%", "_", "  ", "'; DROP TABLE--"));

        for (String kw : specialKeywords) {
            ProductListRequest request = ProductListRequest.newBuilder()
                    .setKeyword(kw)
                    .build();

            assertDoesNotThrow(
                    () -> collectAll(blockingStub.listProducts(request)),
                    "Keyword đặc biệt '" + kw + "' không được gây lỗi server"
            );
        }
    }
}