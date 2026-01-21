package com.flyway.reservation.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerSaveForm {

    @Builder.Default
    private List<PassengerSaveRow> passengers = new ArrayList<>();
}
