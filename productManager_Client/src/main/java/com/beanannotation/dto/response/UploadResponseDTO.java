package com.beanannotation.dto.response;

public class UploadResponseDTO {
    private int count;
    public UploadResponseDTO(int count) {
        this.count = count;
    }
    public int getCount() { return count; }
}
