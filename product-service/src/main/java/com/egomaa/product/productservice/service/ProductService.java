package com.egomaa.product.productservice.service;

import com.egomaa.product.productservice.dto.ProductRequest;
import com.egomaa.product.productservice.dto.ProductResponse;
import com.egomaa.product.productservice.model.Product;
import com.egomaa.product.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository ;

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                                .name(productRequest.getName())
                                .description(productRequest.getDescription())
                                .price(productRequest.getPrice())
                                .build();
        productRepository.save(product);
        log.info("Product {} is saved",product.getId());
    }


    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductResponse> productResponseList = new ArrayList<>();;
        for (Product product:products) {
            ProductResponse productResponse = ProductResponse.builder()
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice()).build();
            productResponseList.add(productResponse);
        }
        return productResponseList;
    }







}
