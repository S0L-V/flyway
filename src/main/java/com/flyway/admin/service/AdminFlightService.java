package com.flyway.admin.service;

import java.util.List;

import com.flyway.search.domain.Flight;

public interface AdminFlightService {

	List<Flight> getFlightList(Flight filter);

	Flight getFlightById(String flightId);

	String createFlight(Flight flight);

	boolean updateFlight(Flight flight);

	boolean deleteFlight(String flightId);
}
