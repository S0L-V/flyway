package com.flyway.reservation.controller;

import com.flyway.reservation.service.ReservationDraftService;
import com.flyway.reservation.domain.DraftCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/dev/reservations")
public class ReservationDraftController {

    private final ReservationDraftService draftService;

    @PostMapping("/draft")
    @ResponseBody
    public Map<String, Object> createDraftDev(
            @RequestParam String outFlightId,
            @RequestParam(required = false) String inFlightId,
            @RequestParam int passengerCount,
            @RequestParam String cabinClassCode,
            @RequestParam String userId
    ) {
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId(outFlightId)
                .inFlightId(inFlightId)
                .passengerCount(passengerCount)
                .cabinClassCode(cabinClassCode)
                .build();

        String reservationId = draftService.createDraft(userId, req);

        Map<String, Object> result = new HashMap<>();
        result.put("reservationId", reservationId);
        result.put("agreementUrl", "/reservations/" + reservationId + "/agreement");
        result.put("bookingUrl", "/reservations/" + reservationId + "/booking");
        return result;
    }
}
