package com.flyway.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class FlightController {

    @GetMapping("/search")
    public String list() {
        return "search/search";
    }
}
