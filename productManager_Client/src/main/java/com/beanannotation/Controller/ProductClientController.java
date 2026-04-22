package com.beanannotation.Controller;

import com.beanannotation.Service.ProductClientService;
import com.beanannotation.dto.response.ProductDto;
import com.google.protobuf.Descriptors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductClientController {
    private final ProductClientService productClientService;

    public ProductClientController(ProductClientService productClientService) {
        this.productClientService = productClientService;
    }

    @GetMapping("/product/{id}")
    public ProductDto getAuthor(@PathVariable String id) {
        return productClientService.getProductById(Integer.parseInt(id));
    }
}
