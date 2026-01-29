package com.flyway.reservation.domain;

import java.util.Locale;

public enum TripType {
    OW,
    RT;

    public static TripType from(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        if (normalized.isEmpty()) return null;
        String upper = normalized.toUpperCase(Locale.ROOT);
        if ("0".equals(upper) || "OW".equals(upper) || "ONE_WAY".equals(upper)) {
            return OW;
        }
        if ("1".equals(upper) || "RT".equals(upper) || "ROUND_TRIP".equals(upper)) {
            return RT;
        }
        return null;
    }
}
