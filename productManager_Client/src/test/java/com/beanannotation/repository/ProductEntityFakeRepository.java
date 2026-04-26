package com.beanannotation.repository;

import com.beanannotation.entity.ProductEntityFake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductEntityFakeRepository extends JpaRepository<ProductEntityFake, Long> {
}
