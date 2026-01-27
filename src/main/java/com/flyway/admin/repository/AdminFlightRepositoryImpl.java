package com.flyway.admin.repository;

import com.flyway.admin.dto.AdminFlightDto;
import com.flyway.admin.mapper.AdminFlightMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminFlightRepositoryImpl implements AdminFlightRepository {

	private final AdminFlightMapper adminFlightMapper;

	@Override
	public List<AdminFlightDto> findFlightList(String departureAirport, String arrivalAirport, int offset, int limit) {
		return adminFlightMapper.selectFlightList(departureAirport, arrivalAirport, offset, limit);
	}

	@Override
	public int countFlights(String departureAirport, String arrivalAirport) {
		return adminFlightMapper.countFlights(departureAirport, arrivalAirport);
	}

	@Override
	public AdminFlightDto findFlightById(String flightId) {
		return adminFlightMapper.selectFlightById(flightId);
	}

	@Override
	public int saveFlight(AdminFlightDto flight) {
		return adminFlightMapper.insertFlight(flight);
	}

	@Override
	public int updateFlight(AdminFlightDto flight) {
		return adminFlightMapper.updateFlight(flight);
	}

	@Override
	public int deleteFlight(String flightId) {
		return adminFlightMapper.deleteFlight(flightId);
	}
}
