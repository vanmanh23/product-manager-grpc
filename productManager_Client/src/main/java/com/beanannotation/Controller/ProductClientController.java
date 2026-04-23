package com.beanannotation.Controller;

import com.beanannotation.ProductResponse;
import com.beanannotation.Service.ProductClientService;
import com.beanannotation.dto.request.UploadRequestDTO;
import com.beanannotation.dto.response.ProductDTO;
import com.beanannotation.dto.response.ProductResponseDTO;
import com.beanannotation.dto.response.UploadResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductClientController {
    private final ProductClientService productClientService;

    public ProductClientController(ProductClientService productClientService) {
        this.productClientService = productClientService;
    }

    @GetMapping("/{id}")
    public ProductDTO getAuthor(@PathVariable String id) {
        return productClientService.getProductById(Integer.parseInt(id));
    }
    @GetMapping("")
    public ResponseEntity<List<ProductResponseDTO>> listProducts(
            @RequestParam(defaultValue = "") String keyword) throws InterruptedException {

        List<ProductResponseDTO> products = productClientService.listProducts(keyword);
        return ResponseEntity.ok(products);
    }
    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadProducts(
            @RequestBody UploadRequestDTO request) throws InterruptedException {

        UploadResponseDTO response =
                productClientService.uploadProducts(request.getProducts());

        return ResponseEntity.ok(response);
    }
    @PostMapping("/chat")
    public ResponseEntity<List<ProductResponseDTO>> chat(
            @RequestBody List<Long> productIds) {

        List<ProductResponseDTO> responses = productClientService.chatProducts(productIds);

        return ResponseEntity.ok(responses);
    }
}
