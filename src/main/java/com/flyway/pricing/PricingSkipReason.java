package com.flyway.pricing;

public final class PricingSkipReason {
    public static final String INVALID_SEATS = "INVALID_SEATS";
    public static final String INVALID_PRICE = "INVALID_PRICE";
    public static final String INVALID_TIME = "INVALID_TIME";
    public static final String COOLDOWN = "COOLDOWN";
    public static final String SMALL_DIFF = "SMALL_DIFF";

    private PricingSkipReason() {}
}
