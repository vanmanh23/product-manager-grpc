package com.beanannotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductItemDTO {
    private Long id;
    private String name;
    private Double price;
}
