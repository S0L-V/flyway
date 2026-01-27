package com.flyway.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.flyway.search.domain.Flight;

@Mapper
public interface AdminFlightMapper {

	/**
	 * 항공편 목록을 조회 (필터 기능)
	 * @param filter 필터 조건 (flightNumber, departureAirport, arrivalAirport)
	 */
	List<Flight> selectFlightList(Flight filter);

	/**
	 * ID로 항공편을 조회
	 */
	Flight selectFlightById(@Param("flightId") String flightId);

	/**
	 * 새로운 항공편을 등록
	 */
	int insertFlight(Flight flight);

	/**
	 * 기존 항공편 정보를 수정
	 */
	int updateFlight(Flight flight);

	/**
	 * 항공편을 삭제
	 */
	int deleteFlight(@Param("flightId") String flightId);
}
