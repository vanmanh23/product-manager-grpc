package com.beanannotation.integration.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UploadRequestDTO {
    @NotEmpty(message = "Products must not be empty")
    @Valid
    private List<ProductItemDTO> products;

    public UploadRequestDTO() {
    }
}
