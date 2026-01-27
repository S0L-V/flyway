package com.flyway.admin.repository;

import java.util.List;

import com.flyway.search.domain.Flight;

public interface AdminFlightRepository {

	List<Flight> findFlightList(Flight filter);

	Flight findFlightById(String flightId);

	int saveFlight(Flight flight);

	int updateFlight(Flight flight);

	int deleteFlight(String flightId);
}
