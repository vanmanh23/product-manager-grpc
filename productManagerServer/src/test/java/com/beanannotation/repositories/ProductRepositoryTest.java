package com.beanannotation.repositories;

import com.beanannotation.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ProductRepository productRepository;
    @BeforeEach
    void setUp() {
    }
    @Test
    void findByNameContainingIgnoreCase() {
        // Given
        ProductEntity product = new ProductEntity();
        product.setName("Laptop HP");
        product.setPrice(1000.0);
        entityManager.persistAndFlush(product);
        // When
        List<ProductEntity> found = productRepository.findByNameContainingIgnoreCase("Laptop HP");
        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Laptop HP");
    }
}