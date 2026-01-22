package com.flyway.search.controller;

import com.flyway.search.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class FlightController {
    @Autowired
    FlightService service;

    @GetMapping("/search")
    public String list() {
        return "search";
    }
}
