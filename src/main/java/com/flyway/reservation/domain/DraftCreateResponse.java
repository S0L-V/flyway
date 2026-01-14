package com.flyway.reservation.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftCreateResponse {
    private String reservationId;
    private String nextUrl;
}
