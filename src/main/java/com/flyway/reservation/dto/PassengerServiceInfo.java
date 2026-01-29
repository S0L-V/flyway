package com.flyway.reservation.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerServiceInfo {
    private String passengerId;
    private String passengerName;
    private String serviceType;      //
    private String serviceName;      //
    private int quantity;
    private long totalPrice;
}
