package com.egomaa.product.productservice.repository;

import com.egomaa.product.productservice.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Long> {

}
