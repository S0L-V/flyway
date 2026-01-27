package com.flyway.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flyway.search.domain.Airport;
import com.flyway.search.service.FlightService;
import com.flyway.template.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/api/utils")
@RequiredArgsConstructor
public class ApiUtilController {

	private final FlightService flightService;

	@GetMapping("/airports")
	public ApiResponse<List<Airport>> getAllAirports() {
		List<Airport> airports = flightService.depAirport(new Airport());
		return ApiResponse.success(airports);
	}
}
