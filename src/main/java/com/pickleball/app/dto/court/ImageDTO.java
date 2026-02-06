package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO {
    private Long imageId;
    private String fileName;
    private String contentType;
    /**
     * Image data encoded as Base64 string.
     */
    private String data;
}

