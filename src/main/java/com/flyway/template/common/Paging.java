package com.flyway.template.common;

import lombok.Getter;

@Getter
public class Paging {
    private final int page;   // 1-based
    private final int size;
    private final int offset;

    private Paging(int page, int size) {
        this.page = page;
        this.size = size;
        this.offset = (page - 1) * size;
    }

    public static Paging of(Integer page, Integer size, int defaultPage, int defaultSize, int maxSize) {
        int p = (page == null) ? defaultPage : Math.max(page, 1);
        int sRaw = (size == null) ? defaultSize : Math.max(size, 1);
        int s = Math.min(sRaw, maxSize);
        return new Paging(p, s);
    }
}
