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
        long offsetLong = (long) (page - 1) * (long) size;
        if (offsetLong < 0) {
            this.offset = 0;
        } else if (offsetLong > Integer.MAX_VALUE) {
            this.offset = Integer.MAX_VALUE;
        } else {
            this.offset = (int) offsetLong;
        }
    }

    public static Paging of(Integer page, Integer size, int defaultPage, int defaultSize, int maxSize) {
        int safeDefaultSize = Math.max(defaultSize, 1);
        int normalizedMaxSize = (maxSize >= 1) ? maxSize : safeDefaultSize;

        int sRaw = (size == null) ? safeDefaultSize : Math.max(size, 1);
        int s = Math.min(sRaw, normalizedMaxSize);
        int safeSize = Math.max(s, 1);

        int pRaw = (page == null) ? defaultPage : page;
        int safePage = Math.max(pRaw, 1);
        int maxPage = Math.max(1, Integer.MAX_VALUE / safeSize);
        int cappedPage = Math.min(safePage, maxPage);

        return new Paging(cappedPage, safeSize);
    }
}
