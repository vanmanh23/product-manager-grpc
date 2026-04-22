package com.beanannotation.service;

import com.beanannotation.Product;

import java.util.ArrayList;
import java.util.List;

public class productsDB {
    public static List<Product> getProductsFromProductDb() {
        return new ArrayList<Product>() {
            {
                add(Product.newBuilder().setId(1).setName("Laptop").setPrice(1000).build());
                add(Product.newBuilder().setId(2).setName("Phone").setPrice(500).build());
                add(Product.newBuilder().setId(3).setName("Tablet").setPrice(300).build());
            }
        };
    }
}
