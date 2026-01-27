package com.pickleball.app.dto.court;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response cho search theo slot: Mỗi slot là một kết quả riêng biệt
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourtSlotSearchResponse {
    private List<CourtSlotSearchResult> results;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;
}

