package com.flyway.admin.service;

import com.flyway.admin.dto.AdminFlightDto;

import java.util.List;

public interface AdminFlightService {

	List<AdminFlightDto> getFlightList(String departureAirport, String arrivalAirport, int page, int size);

	int getFlightCount(String departureAirport, String arrivalAirport);

	AdminFlightDto getFlightById(String flightId);

	String createFlight(AdminFlightDto flight);

	boolean updateFlight(AdminFlightDto flight);

	boolean deleteFlight(String flightId);
}
