package com.flyway.admin.repository;

import com.flyway.admin.dto.AdminFlightDto;

import java.util.List;

public interface AdminFlightRepository {

	List<AdminFlightDto> findFlightList(String departureAirport, String arrivalAirport, int offset, int limit);

	int countFlights(String departureAirport, String arrivalAirport);

	AdminFlightDto findFlightById(String flightId);

	int saveFlight(AdminFlightDto flight);

	int updateFlight(AdminFlightDto flight);

	int deleteFlight(String flightId);
}
