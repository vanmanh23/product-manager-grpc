package com.beanannotation.dto.response;

import com.beanannotation.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponseDTO {
    private List<ProductResponse> products;
    private int total;
}
