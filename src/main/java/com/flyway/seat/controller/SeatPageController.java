package com.flyway.seat.controller;

import com.flyway.seat.service.SeatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservations")
public class SeatPageController {

    private final SeatService seatService; // 주입 받을 필드

    public SeatPageController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{reservationId}/segments/{segmentId}/seats")
    public String seatSelectPage(@PathVariable String reservationId,
                                 @PathVariable("segmentId") String reservationSegmentId,
                                 Model model) {

        // 실제 DB에서 passenger 1명 조회
        String passengerId = seatService.findFirstPassengerId(reservationId);

        model.addAttribute("reservationId", reservationId);
        model.addAttribute("reservationSegmentId", reservationSegmentId);
        model.addAttribute("passengerId", passengerId);

        return "seat/seat-select"; // JSP 경로 유지
    }
}
