package com.flyway.reservation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

@Controller
@RequestMapping("/reservation/{reservationId}")
public class ReservationAgreementController {

    private static final  String AGREED_RESERVATION_IDS = "reservationId";

    @GetMapping("/agreement")
    public String AgreementPage(@PathVariable String reservationId) {
        return "reservation/agreement";
    }

    @PostMapping("/agreement")
    public String submitAgreement(
            @PathVariable String reservationId,
            @RequestParam(name = "agreeAll", defaultValue = "false") boolean agreeAll,
            HttpSession session
    ) {
        if (!agreeAll) {
            return "redirect:/reservation" + reservationId + "/agreement?error= agreeRequired";
        }

        Set<String> agreed = getOrCreateAgreedSet(session);
        agreed.add(reservationId);
        session.setAttribute(AGREED_RESERVATION_IDS, agreed);

        return "redirect:/reservation/" + reservationId + "/booking";
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