package com.beanannotation.integration.repository;

import com.beanannotation.integration.entity.ProductEntityFake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductEntityFakeRepository extends JpaRepository<ProductEntityFake, Long> {
}
