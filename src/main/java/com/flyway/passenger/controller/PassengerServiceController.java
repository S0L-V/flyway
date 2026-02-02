package com.flyway.passenger.controller;

import com.flyway.passenger.dto.BaggageSaveRequest;
import com.flyway.reservation.dto.*;
import com.flyway.passenger.service.PassengerServiceService;
import com.flyway.reservation.dto.ServicePopupViewModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class PassengerServiceController {

    private final PassengerServiceService serviceService;


    //부가서비스 팝업 페이지
    @GetMapping("/{reservationId}/services")
    public String servicePopup(@PathVariable String reservationId, Model model) {
        String userId = getAuthenticatedUserIdOrThrow();
        ServicePopupViewModel vm = serviceService.getServicePopup(reservationId, userId);
        model.addAttribute("vm", vm);
        return "reservations/service-popup";
    }


     //수하물 저장
    @PostMapping("/{reservationId}/services/baggage")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBaggage(
            @PathVariable String reservationId,
            @RequestBody BaggageSaveRequest request
    ) {
        String userId = getAuthenticatedUserIdOrThrow();
        serviceService.saveBaggage(reservationId, userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }


    // 기내식 저장
    @PostMapping("/{reservationId}/services/meal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveMeal(
            @PathVariable String reservationId,
            @RequestBody MealSaveRequest request
    ) {
        String userId = getAuthenticatedUserIdOrThrow();
        serviceService.saveMeal(reservationId, userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    //부가서비스 총액 조회
    @GetMapping("/{reservationId}/services/total")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceTotal(@PathVariable String reservationId) {
        String userId = getAuthenticatedUserIdOrThrow();
        Long total = serviceService.getServiceTotal(reservationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", total);
        return ResponseEntity.ok(result);
    }
    // 부가서비스 상세 정보 조회 (AJAX)
    @GetMapping("/{reservationId}/services/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getServiceDetails(@PathVariable String reservationId) {
        String userId = getAuthenticatedUserIdOrThrow();

        // 기존 서비스 로직 활용
        ServicePopupViewModel vm = serviceService.getServicePopup(reservationId, userId);

        List<Map<String, Object>> segmentServices = new ArrayList<>();
        for (var segment : vm.getSegments()) {
            Map<String, Object> segData = new HashMap<>();
            segData.put("segmentOrder", segment.getSegmentOrder());

            // 해당 구간의 서비스 정보 수집
            List<Map<String, Object>> baggageList = new ArrayList<>();
            List<Map<String, Object>> mealList = new ArrayList<>();

            for (var pax : vm.getPassengers()) {
                // 수하물
                if (pax.getBaggageServices() != null) {
                    for (var bs : pax.getBaggageServices()) {
                        if (segment.getReservationSegmentId().equals(bs.getReservationSegmentId())) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("passengerName", pax.getFirstName() + " " + pax.getLastName());
                            item.put("serviceName", bs.getServiceDetails()); // JSON 형태
                            baggageList.add(item);
                        }
                    }
                }
                // 기내식
                if (pax.getMealServices() != null) {
                    for (var ms : pax.getMealServices()) {
                        if (segment.getReservationSegmentId().equals(ms.getReservationSegmentId())) {
                            Map<String, Object> item = new HashMap<>();
                            item.put("passengerName", pax.getFirstName() + " " + pax.getLastName());
                            item.put("serviceName", ms.getMealName());
                            mealList.add(item);
                        }
                    }
                }
            }

            segData.put("baggageServices", baggageList);
            segData.put("mealServices", mealList);
            segmentServices.add(segData);
        }

        Long total = serviceService.getServiceTotal(reservationId, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("total", total);
        result.put("segments", segmentServices);
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