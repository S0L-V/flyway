package com.flyway.admin.service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.flyway.admin.repository.AdminFlightRepository;
import com.flyway.search.domain.Flight;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFlightServiceImpl implements AdminFlightService {

	private final AdminFlightRepository adminFlightRepository;

	@Override
	@Transactional(readOnly = true)
	public List<Flight> getFlightList(Flight filter) {
		try {
			return adminFlightRepository.findFlightList(filter);
		} catch (Exception e) {
			log.error("Failed to get flight list with filter: {}", filter, e);
			return Collections.emptyList();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Flight getFlightById(String flightId) {
		try {
			return adminFlightRepository.findFlightById(flightId);
		} catch (Exception e) {
			log.error("Failed to get flight by ID: {}", flightId, e);
			return null;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public String createFlight(Flight flight) {
		try {
			String flightId = UUID.randomUUID().toString();
			flight.setFlightId(flightId);
			int result = adminFlightRepository.saveFlight(flight);
			if (result > 0) {
				log.info("Flight created: {}", flight);
				return flightId;
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to crate flight: {}", flight, e);
			return null;
		}
	}

	@Override
	@Transactional
	public boolean updateFlight(Flight flight) {
		try {
			int result = adminFlightRepository.updateFlight(flight);
			if (result > 0) {
				log.info("Flight updated: {}", flight.getFlightId());
				return true;
			}
			return true;
		} catch (Exception e) {
			log.error("Failed to update flight: {}", flight, e);
			return false;
		}
	}

	@Override
	@Transactional
	public boolean deleteFlight(String flightId) {
		try {
			int result = adminFlightRepository.deleteFlight(flightId);
			if (result > 0) {
				log.info("Flight deleted: {}", flightId);
				return true;
			}
			return false;
		} catch (Exception e) {
			log.error("Failed to delete flight: {}", flightId, e);
			return false;
		}
	}
}
