package com.flyway.admin.controller;

import com.flyway.admin.dto.AdminFlightDto;
import com.flyway.admin.service.AdminFlightService;
import com.flyway.template.common.ApiResponse;
import com.flyway.template.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/flights")
@RequiredArgsConstructor
@Slf4j
public class AdminFlightController {

	private final AdminFlightService adminFlightService;

	// Flight 조회 (Filter + 페이징)
	@GetMapping
	public ApiResponse<Map<String, Object>> getFlightList(
		@RequestParam(required = false) String departureAirport,
		@RequestParam(required = false) String arrivalAirport,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		try {
			List<AdminFlightDto> flights = adminFlightService.getFlightList(departureAirport, arrivalAirport, page, size);
			int totalCount = adminFlightService.getFlightCount(departureAirport, arrivalAirport);

			Map<String, Object> responseData = new HashMap<>();
			responseData.put("list", flights);
			responseData.put("totalCount", totalCount);
			responseData.put("currentPage", page);
			responseData.put("pageSize", size);
			responseData.put("totalPages", (int) Math.ceil((double) totalCount / size));

			return ApiResponse.success(responseData);
		} catch (Exception e) {
			log.error("Failed to get flight list - departure: {}, arrival: {}", departureAirport, arrivalAirport, e);
			return ApiResponse.error(ErrorCode.FLIGHT_LIST_FETCH_FAILED.getCode(), ErrorCode.FLIGHT_LIST_FETCH_FAILED.getMessage());
		}
	}

	// Flight 상세 조회
	@GetMapping("/{id}")
	public ApiResponse<AdminFlightDto> getFlightById(@PathVariable String id) {
		try {
			AdminFlightDto flight = adminFlightService.getFlightById(id);
			if (flight == null) {
				return ApiResponse.error(ErrorCode.FLIGHT_NOT_FOUND.getCode(), ErrorCode.FLIGHT_NOT_FOUND.getMessage());
			}
			return ApiResponse.success(flight);
		} catch (Exception e) {
			log.error("Failed to get flight by ID: {}", id, e);
			return ApiResponse.error(ErrorCode.FLIGHT_NOT_FOUND.getCode(), ErrorCode.FLIGHT_NOT_FOUND.getMessage());
		}
	}

	// Flight 생성
	@PostMapping
	public ApiResponse<String> createFlight(@RequestBody AdminFlightDto flight) {
		try {
			String flightId = adminFlightService.createFlight(flight);
			if (flightId == null) {
				return ApiResponse.error(ErrorCode.FLIGHT_CREATION_FAILED.getCode(), ErrorCode.FLIGHT_CREATION_FAILED.getMessage());
			}
			return ApiResponse.success(flightId, "항공편이 성공적으로 등록되었습니다.");
		} catch (Exception e) {
			log.error("Failed to create flight: {}", flight, e);
			return ApiResponse.error(ErrorCode.FLIGHT_CREATION_FAILED.getCode(), ErrorCode.FLIGHT_CREATION_FAILED.getMessage());
		}
	}

	// Flight 수정
	@PutMapping("/{id}")
	public ApiResponse<Void> updateFlight(@PathVariable String id, @RequestBody AdminFlightDto flight) {
		try {
			flight.setFlightId(id);
			boolean success = adminFlightService.updateFlight(flight);
			if (!success) {
				return ApiResponse.error(ErrorCode.FLIGHT_UPDATED_FAILED.getCode(), ErrorCode.FLIGHT_UPDATED_FAILED.getMessage());
			}
			return ApiResponse.success(null, "항공편이 성공적으로 수정되었습니다.");
		} catch (Exception e) {
			log.error("Failed to update flight: {}", flight, e);
			return ApiResponse.error(ErrorCode.FLIGHT_UPDATED_FAILED.getCode(), ErrorCode.FLIGHT_UPDATED_FAILED.getMessage());
		}
	}

	// Flight 삭제
	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteFlight(@PathVariable String id) {
		try {
			boolean success = adminFlightService.deleteFlight(id);
			if (!success) {
				return ApiResponse.error(ErrorCode.FLIGHT_DELETE_FAILED.getCode(), ErrorCode.FLIGHT_DELETE_FAILED.getMessage());
			}
			return ApiResponse.success(null, "항공편이 성공적으로 삭제되었습니다.");
		} catch (Exception e) {
			log.error("Failed to delete flight: {}", id, e);
			return ApiResponse.error(ErrorCode.FLIGHT_DELETE_FAILED.getCode(), ErrorCode.FLIGHT_DELETE_FAILED.getMessage());
		}
	}
}