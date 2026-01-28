package com.flyway.template.common;

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
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);

        int totalPages = (int) Math.ceil((double) totalElements / (double) safeSize);
        boolean hasPrevious = safePage > 1 && totalPages > 0;
        boolean hasNext = safePage < totalPages;

        return new PageInfo(safePage, safeSize, totalElements, totalPages, hasNext, hasPrevious);
    }
}
