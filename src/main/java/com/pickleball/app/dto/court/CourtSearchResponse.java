package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtSearchResponse {
    private List<CourtDTO> courts;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
}



