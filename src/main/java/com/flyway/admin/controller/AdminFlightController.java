package com.flyway.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flyway.admin.service.AdminFlightService;
import com.flyway.search.domain.Flight;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/admin/api/flights")
@RequiredArgsConstructor
@Slf4j
public class AdminFlightController {

	private final AdminFlightService adminFlightService;

	@GetMapping
	public ApiResponse<List<Flight>> getFlightList(@ModelAttribute Flight filter) {
		try {
			List<Flight> flights = adminFlightService.getFlightList(filter);
			return ApiResponse.success(flights);
		} catch (Exception e) {
			log.error("Failed to get flight list with filter: {}", filter, e);
			return ApiResponse.error(ErrorCode.FLIGHT_LIST_FETCH_FAILED.getCode(),
				ErrorCode.FLIGHT_LIST_FETCH_FAILED.getMessage());
		}
	}

	@GetMapping("/{id}")
	public ApiResponse<Flight> getFlightById(@PathVariable String id) {
		try {
			Flight flight = adminFlightService.getFlightById(id);
			if (flight == null) {
				return ApiResponse.error(ErrorCode.FLIGHT_NOT_FOUND.getCode(), ErrorCode.FLIGHT_NOT_FOUND.getMessage());
			}
			return ApiResponse.success(flight);
		} catch (Exception e) {
			log.error("Failed to get flight by ID: {}", id, e);
			return ApiResponse.error(ErrorCode.FLIGHT_NOT_FOUND.getCode(), ErrorCode.FLIGHT_NOT_FOUND.getMessage());
		}
	}

	@PutMapping("/{id}")
	public ApiResponse<Void> updateFlight(@PathVariable String id, @RequestBody Flight flight) {
		try {
			flight.setFlightId(id);
			boolean success = adminFlightService.updateFlight(flight);
			if (!success) {
				return ApiResponse.error(ErrorCode.FLIGHT_UPDATED_FAILED.getCode(),
					ErrorCode.FLIGHT_UPDATED_FAILED.getMessage());
			}
			return ApiResponse.success(null, "항공편이 성공적으로 수정되었습니다.");
		} catch (Exception e) {
			log.error("Failed to update flight: {}", flight, e);
			return ApiResponse.error(ErrorCode.FLIGHT_UPDATED_FAILED.getCode(),
				ErrorCode.FLIGHT_UPDATED_FAILED.getMessage());
		}
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteFlight(@PathVariable String id) {
		try {
			boolean success = adminFlightService.deleteFlight(id);
			if (!success) {
				return ApiResponse.error(ErrorCode.FLIGHT_DELETE_FAILED.getCode(),
					ErrorCode.FLIGHT_DELETE_FAILED.getMessage());
			}
			return ApiResponse.success(null, "항공편이 성공적으로 삭제되었습니다.");
		} catch (Exception e) {
			log.error("Failed to delete flight: {}", id, e);
			return ApiResponse.error(ErrorCode.PROMO_DELETE_FAILED.getCode(),
				ErrorCode.FLIGHT_DELETE_FAILED.getMessage());
		}
	}


}
