package com.flyway.common.domain;

import java.util.Locale;

public enum CabinClass {
    ECO("ECONOMY"),
    BIZ("BUSINESS"),
    FST("FIRST");

    private final String apiValue;

    CabinClass(String apiValue) {
        this.apiValue = apiValue;
    }

    public String toApiValue() {
        return apiValue;
    }

    public static CabinClass fromCode(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        String upper = v.toUpperCase(Locale.ROOT);
        for (CabinClass c : values()) {
            if (c.name().equals(upper)) {
                return c;
            }
        }
        return null;
    }
}
