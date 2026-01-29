package com.flyway.reservation.controller;

import com.flyway.reservation.dto.BookingViewModel;
import com.flyway.passenger.dto.PassengerSaveForm;
import com.flyway.reservation.service.ReservationBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationBookingController {

    private final ReservationBookingService bookingService;

    @GetMapping("/{reservationId}/booking")
    public String bookingPage(@PathVariable String reservationId, Model model) {

        String userId = getAuthenticatedUserIdOrThrow();

        BookingViewModel vm = bookingService.getBookingView(reservationId, userId);
        model.addAttribute("vm", vm);

        return "reservations/booking";
    }

    @PostMapping("/{reservationId}/passengers")
    public String savePassengers(
            @PathVariable String reservationId,
            @ModelAttribute PassengerSaveForm form
    ) {
        String userId = getAuthenticatedUserIdOrThrow();

        bookingService.savePassengers(reservationId, userId, form.getPassengers());

        return "redirect:/reservations/" + reservationId + "/booking?saved=1";
    }
//json형식
    @PostMapping("/{reservationId}/passengers/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> savePassengersApi(
            @PathVariable String reservationId,
            @RequestBody PassengerSaveForm form
    ) {
        String userId = getAuthenticatedUserIdOrThrow();
        bookingService.savePassengers(reservationId, userId, form.getPassengers());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    private String getAuthenticatedUserIdOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
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
        } catch (Exception ignored) {}

        String name = auth.getName();
        if (name == null || name.trim().isEmpty() || "anonymousUser".equalsIgnoreCase(name.trim())) {
            throw new IllegalStateException("Cannot resolve userId from Authentication");
        }
        return name.trim();
    }
}
