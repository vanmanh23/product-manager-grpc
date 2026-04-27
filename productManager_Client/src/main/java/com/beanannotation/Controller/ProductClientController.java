package com.beanannotation.Controller;

import com.beanannotation.Service.ProductClientService;
import com.beanannotation.dto.request.UploadRequestDTO;
import com.beanannotation.dto.response.ProductDTO;
import com.beanannotation.dto.response.UploadResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductClientController {
    private final ProductClientService productClientService;

    public ProductClientController(ProductClientService productClientService) {
        this.productClientService = productClientService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable String id) {
        return ResponseEntity.ok(productClientService.getProductById(Integer.parseInt(id)));
    }

    @GetMapping("")
    public ResponseEntity<List<ProductDTO>> listProducts(
            @RequestParam(defaultValue = "") String keyword) throws InterruptedException {

        List<ProductDTO> products = productClientService.listProducts(keyword);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDTO> uploadProducts(@Valid
                                                            @RequestBody UploadRequestDTO request) throws InterruptedException {

        UploadResponseDTO response =
                productClientService.uploadProducts(request.getProducts());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bidirectional")
    public ResponseEntity<List<ProductDTO>> productBidirectionalStreaming(
            @RequestBody List<Long> productIds) {

        List<ProductDTO> responses = productClientService.bidirectionalStreamingProducts(productIds);

        return ResponseEntity.ok(responses);
    }
}
