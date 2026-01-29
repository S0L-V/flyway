package com.flyway.template.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class PageInfo {
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PageInfo(int page, int size, long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public static PageInfo of(int page, int size, long totalElements) {
        int safeSize = Math.max(size, 1);
        int totalPages = (int) Math.ceil((double) totalElements / (double) safeSize);
        int safePage = Math.max(1, Math.min(page, totalPages == 0 ? 1 : totalPages));
        boolean hasPrevious = safePage > 1 && totalPages > 0;
        boolean hasNext = safePage < totalPages;

        return new PageInfo(safePage, safeSize, totalElements, totalPages, hasNext, hasPrevious);
    }

    @JsonIgnore
    public int getOffset() {
        return (page - 1) * size;
    }
}
