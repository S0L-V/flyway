package com.flyway.admin.service;

import com.flyway.admin.dto.AdminFlightDto;
import com.flyway.admin.repository.AdminFlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminFlightServiceImpl implements AdminFlightService {

	private final AdminFlightRepository adminFlightRepository;

	@Override
	@Transactional(readOnly = true)
	public List<AdminFlightDto> getFlightList(String departureAirport, String arrivalAirport, int page, int size) {
		try {
			int safePage = Math.max(1, page);
			int safeSize = Math.max(1, Math.min(size, 100));
			int offset = (safePage - 1) * size;
			return adminFlightRepository.findFlightList(departureAirport, arrivalAirport, offset, safeSize);
		} catch (Exception e) {
			log.error("Failed to get flight list - departure: {}, arrival: {}", departureAirport, arrivalAirport, e);
			return Collections.emptyList();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public int getFlightCount(String departureAirport, String arrivalAirport) {
		try {
			return adminFlightRepository.countFlights(departureAirport, arrivalAirport);
		} catch (Exception e) {
			log.error("Failed to count flights - departure: {}, arrival: {}", departureAirport, arrivalAirport, e);
			return 0;
		}
	}

	@Override
	@Transactional(readOnly = true)
	public AdminFlightDto getFlightById(String flightId) {
		try {
			return adminFlightRepository.findFlightById(flightId);
		} catch (Exception e) {
			log.error("Failed to get flight by ID: {}", flightId, e);
			return null;
		}
	}

	@Override
	@Transactional
	public String createFlight(AdminFlightDto flight) {
		try {
			String flightId = UUID.randomUUID().toString();
			flight.setFlightId(flightId);
			int result = adminFlightRepository.saveFlight(flight);
			if (result > 0) {
				log.info("Flight created: {}", flightId);
				return flightId;
			}
			return null;
		} catch (Exception e) {
			log.error("Failed to create flight: {}", flight, e);
			return null;
		}
	}

	@Override
	@Transactional
	public boolean updateFlight(AdminFlightDto flight) {
		try {
			int result = adminFlightRepository.updateFlight(flight);
			if (result > 0) {
				log.info("Flight updated: {}", flight.getFlightId());
				return true;
			}
			return false;
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
