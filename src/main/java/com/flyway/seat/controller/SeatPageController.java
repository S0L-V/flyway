package com.flyway.seat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reservations")
public class SeatPageController {

    @GetMapping("/{reservationId}/segments/{segmentId}/seats")
    public String seatSelectPage(@PathVariable String reservationId,
                                 @PathVariable("segmentId") String reservationSegmentId,
                                 Model model) {

        model.addAttribute("reservationId", reservationId);
        model.addAttribute("reservationSegmentId", reservationSegmentId);

        return "seat/seat-select"; // JSP 경로 유지
    }
}
