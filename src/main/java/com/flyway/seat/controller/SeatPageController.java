package com.flyway.seat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SeatPageController {

    @GetMapping("/seat/select")
    public String seatSelectPage() {
        return "seat/seat-select";
    }
}
