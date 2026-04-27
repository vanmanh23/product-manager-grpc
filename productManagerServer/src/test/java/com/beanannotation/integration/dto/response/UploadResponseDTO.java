package com.beanannotation.integration.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UploadResponseDTO {
    private int count;
    public UploadResponseDTO(int count) {
        this.count = count;
    }
    public int getCount() { return count; }
}
