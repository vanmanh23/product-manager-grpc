package com.beanannotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequestDTO {
    @NotEmpty(message = "Products must not be empty")
    @Valid
    private List<ProductItemDTO> products;
}
