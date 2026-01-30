package com.flyway.reservation.controller;

import com.flyway.reservation.dto.ReservationSegmentView;
import com.flyway.reservation.repository.ReservationBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/reservations/{reservationId}")
@RequiredArgsConstructor
public class ReservationAgreementController {

    private static final  String AGREED_RESERVATION_IDS = "reservationId";
    private final ReservationBookingRepository bookingRepository;

    @GetMapping("/agreement")
    public String agreementPage(@PathVariable String reservationId, Model model) {
        List<ReservationSegmentView> segments = bookingRepository.findSegments(reservationId);
        model.addAttribute("segments", segments);
        return "reservations/agreement";
    }
    @PostMapping("/agreement")
    public String submitAgreement(
            @PathVariable String reservationId,
            @RequestParam(name = "agreeAll", defaultValue = "false") boolean agreeAll,
            HttpSession session
    ) {
        if (!agreeAll) {
            return "redirect:/reservations/" + reservationId + "/agreement?error=agreeRequired";
        }

        Set<String> agreed = getOrCreateAgreedSet(session);
        agreed.add(reservationId);
        session.setAttribute(AGREED_RESERVATION_IDS, agreed);

        return "redirect:/reservations/" + reservationId + "/booking";
    }
    @SuppressWarnings("unchecked")
    private Set<String> getOrCreateAgreedSet(HttpSession session) {
        Object obj = session.getAttribute(AGREED_RESERVATION_IDS);
        if (obj instanceof Set) {
            return (Set<String>) obj;
        }
        return new HashSet<>();
    }

}