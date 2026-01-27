package com.flyway.reservation.controller;

import com.flyway.reservation.domain.DraftCreateRequest;
import com.flyway.reservation.domain.DraftCreateResponse;
import com.flyway.reservation.service.ReservationDraftService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationDraftController {

    private final ReservationDraftService draftService;


    //예약 초안 생성
    @PostMapping("/draft")
    public String createDraft(
            @RequestParam String outFlightId,
            @RequestParam(required = false) String inFlightId,
            @RequestParam int passengerCount,
            @RequestParam String cabinClassCode,
            @RequestParam Long outPrice,
            @RequestParam(required = false) Long inPrice
    ) {
        //  SecurityContext에서 userId 추출
        String userId = getAuthenticatedUserIdOrThrow();

        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId(outFlightId)
                .inFlightId(inFlightId)
                .passengerCount(passengerCount)
                .cabinClassCode(cabinClassCode)
                .outPrice(outPrice)
                .inPrice(inPrice)
                .build();

        String reservationId = draftService.createDraft(userId, req);

        return "redirect:/reservations/" + reservationId + "/agreement";
    }


    private String getAuthenticatedUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated: authentication is null or not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof String) {
            String v = ((String) principal).trim();
            if (!v.isEmpty() && !"anonymousUser".equalsIgnoreCase(v)) {
                return v;
            }
        }

        try {
            Method m = principal.getClass().getMethod("getUserId");
            Object v = m.invoke(principal);
            if (v != null && !v.toString().trim().isEmpty()) {
                return v.toString().trim();
            }
        } catch (Exception ignored) {
        }

        String name = auth.getName();
        if (name == null || name.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(name.trim())) {
            throw new IllegalStateException("Cannot resolve userId from Authentication");
        }
        return name.trim();
    }


    /** 예외처리는 명세서 있어서 굳이 필요 없음
    String normalizeCabinClassCode(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toUpperCase();

        if ("ECO".equals(v) || "BIZ".equals(v) || "FST".equals(v)) return v;
        if ("ECONOMY".equals(v) || "ECONOMY_CLASS".equals(v)) return "ECO";
        if ("BUSINESS".equals(v) || "BUSINESS_CLASS".equals(v)) return "BIZ";
        if ("FIRST".equals(v) || "FIRST_CLASS".equals(v)) return "FST";

        return v;
    }
   **/

}

