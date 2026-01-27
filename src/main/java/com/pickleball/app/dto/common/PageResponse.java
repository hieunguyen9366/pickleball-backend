package com.pickleball.app.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private Long total;
    private Integer totalPages;
    private Integer currentPage;
    private Integer size;
    private List<T> content;
}
