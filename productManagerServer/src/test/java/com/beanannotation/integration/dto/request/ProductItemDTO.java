package com.beanannotation.integration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemDTO {
    private Long id;
    @NotBlank
    @Size(min = 6, message = "Product name must be at least 6 characters")
    private String name;
    private Double price;
}
