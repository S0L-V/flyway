package com.flyway.search.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultDTO {
    private List<FlightOptionDTO> options;
}
