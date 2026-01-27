package com.flyway.admin.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.flyway.admin.mapper.AdminFlightMapper;
import com.flyway.search.domain.Flight;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AdminFlightRepositoryImpl implements AdminFlightRepository {

	private final AdminFlightMapper adminFlightMapper;

	@Override
	public List<Flight> findFlightList(Flight filter) {
		return adminFlightMapper.selectFlightList(filter);
	}

	@Override
	public Flight findFlightById(String flightId) {
		return adminFlightMapper.selectFlightById(flightId);
	}

	@Override
	public int saveFlight(Flight flight) {
		return adminFlightMapper.insertFlight(flight);
	}

	@Override
	public int updateFlight(Flight flight) {
		return adminFlightMapper.updateFlight(flight);
	}

	@Override
	public int deleteFlight(String flightId) {
		return adminFlightMapper.deleteFlight(flightId);
	}
}
