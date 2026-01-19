package com.flyway.search.mapper;

import com.flyway.search.domain.*;
import com.flyway.search.dto.FlightSearchRequestDTO;
import com.flyway.search.dto.FlightSearchResponseDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlightMapper {
    List<FlightVO> list(FlightVO vo);
    List<AirportVO> airport(AirportVO vo);
    List<FlightSearchResponseDTO> outbound(FlightSearchRequestDTO dto);
    List<FlightSearchResponseDTO> inbound(FlightSearchRequestDTO dto);
}
