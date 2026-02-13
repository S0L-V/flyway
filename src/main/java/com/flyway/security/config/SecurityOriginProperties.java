package com.flyway.security.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Component
public class SecurityOriginProperties {

    private static final String FALLBACK_ALLOWED_ORIGIN = "https://flyway.kr";

    private final List<String> allowedOrigins;

    public SecurityOriginProperties(@Value("${security.allowed-origins:}") String allowedOriginsRaw) {
        this.allowedOrigins = parseAllowedOrigins(allowedOriginsRaw);
    }

    private List<String> parseAllowedOrigins(String raw) {
        Set<String> values = new LinkedHashSet<>();
        if (StringUtils.hasText(raw)) {
            String[] tokens = raw.split(",");
            for (String token : tokens) {
                if (StringUtils.hasText(token)) {
                    values.add(token.trim());
                }
            }
        }

        if (values.isEmpty()) {
            values.add(FALLBACK_ALLOWED_ORIGIN);
        }

        return Collections.unmodifiableList(new ArrayList<>(values));
    }
}
