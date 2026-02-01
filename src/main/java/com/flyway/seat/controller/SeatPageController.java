package com.flyway.seat.controller;

import com.flyway.seat.dto.SegmentCardDTO;
import com.flyway.seat.service.SeatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/reservations")
public class SeatPageController {

    private final SeatService seatService; // 주입 받을 필드

    public SeatPageController(SeatService seatService) {
        this.seatService = seatService;
    }

    @GetMapping("/{reservationId}/segments/{segmentId}/seats")
    public String seatSelectPage(
            @PathVariable String reservationId,
            @PathVariable("segmentId") String reservationSegmentId,
            Model model) {

        String passengerId = seatService.findFirstPassengerId(reservationId);
        String cabinClassCode = seatService.findCabinClassCode(reservationSegmentId);
        List<SegmentCardDTO> segments = seatService.getSegmentCards(reservationId);

        model.addAttribute("reservationId", reservationId);
        model.addAttribute("reservationSegmentId", reservationSegmentId);
        model.addAttribute("passengerId", passengerId);
        model.addAttribute("segments", segments);
        model.addAttribute("cabinClassCode", cabinClassCode);

        // 다인원용
        model.addAttribute("passengers", seatService.findPassengers(reservationId));

        return "seat/seat-select";
    }
}