package com.flyway.admin.mapper;

import com.flyway.admin.dto.AdminFlightDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminFlightMapper {

	/**
	 * 항공편 목록을 조회합니다 (필터 가능, 페이징).
	 */
	List<AdminFlightDto> selectFlightList(
		@Param("departureAirport") String departureAirport,
		@Param("arrivalAirport") String arrivalAirport,
		@Param("offset") int offset,
		@Param("limit") int limit
	);

	/**
	 * 항공편 총 건수를 조회합니다 (필터 적용).
	 */
	int countFlights(
		@Param("departureAirport") String departureAirport,
		@Param("arrivalAirport") String arrivalAirport
	);

	/**
	 * ID로 항공편을 조회합니다.
	 */
	AdminFlightDto selectFlightById(@Param("flightId") String flightId);

	/**
	 * 새로운 항공편을 등록합니다.
	 */
	int insertFlight(AdminFlightDto flight);

	/**
	 * 기존 항공편 정보를 수정합니다.
	 */
	int updateFlight(AdminFlightDto flight);

	/**
	 * 항공편을 삭제합니다.
	 */
	int deleteFlight(@Param("flightId") String flightId);
}
