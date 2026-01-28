package com.flyway.template.common;

import java.util.List;

import lombok.Getter;

@Getter
public class PageResult<T> {
    private final List<T> data;
    private final PageInfo page;

    public PageResult(List<T> data, PageInfo page) {
        this.data = data;
        this.page = page;
    }

    public static <T> PageResult<T> of(List<T> items, PageInfo pageInfo) {
        return new PageResult<>(items, pageInfo);
    }
}
