package com.flyway.pricing.policy;

public interface PricingPolicy {
    String version();

    double mLoad(double r);
    double mTimeDay(long d);
    double mTimeHour(long h);

    double alphaDay(long d);
    double alphaHour(long h);
}

