package com.flyway.reservation.service;

import com.flyway.common.domain.CabinClass;
import com.flyway.reservation.dto.ReservationPaymentDetailDto;
import com.flyway.reservation.dto.ReservationPaymentResponseDto;
import com.flyway.reservation.dto.ReservationPaymentResponseDto.Fare;
import com.flyway.reservation.dto.ReservationPaymentResponseDto.FareSegment;
import com.flyway.reservation.dto.ReservationPaymentResponseDto.ServiceItem;
import com.flyway.reservation.repository.ReservationPaymentQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationPaymentQueryServiceImpl implements ReservationPaymentQueryService {

    private final ReservationPaymentQueryRepository reservationPaymentQueryRepository;

    @Override
    public ReservationPaymentResponseDto getReservationPaymentDetail(String reservationId) {
        ReservationPaymentDetailDto row = reservationPaymentQueryRepository
                .findReservationPaymentDetail(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 결제 상세를 찾을 수 없습니다. reservationId=" + reservationId));

        FareSegment outbound = FareSegment.builder()
                .flightNumber(row.getOutFlightNumber())
                .cabinClass(toCabinClassApiValue(row.getOutCabinClassCode(), row.getOutCabinClass()))
                .pricePerPerson(row.getOutPricePerPerson())
                .passengerCount(row.getPassengerCount())
                .total(row.getOutTotal())
                .build();

        FareSegment inbound = null;
        if (row.getInFlightNumber() != null) {
            inbound = FareSegment.builder()
                    .flightNumber(row.getInFlightNumber())
                    .cabinClass(toCabinClassApiValue(row.getInCabinClassCode(), row.getInCabinClass()))
                    .pricePerPerson(row.getInPricePerPerson())
                    .passengerCount(row.getPassengerCount())
                    .total(row.getInTotal())
                    .build();
        }

        List<ServiceItem> services = new ArrayList<>();
        if (row.getBaggageTotal() != null && row.getBaggageTotal() > 0) {
            services.add(ServiceItem.builder()
                    .name("수하물")
                    .amount(row.getBaggageTotal())
                    .build());
        }
        if (row.getMealTotal() != null && row.getMealTotal() > 0) {
            services.add(ServiceItem.builder()
                    .name("기내식")
                    .amount(row.getMealTotal())
                    .build());
        }

        Long totalAmount = row.getTotalAmount();

        return ReservationPaymentResponseDto.builder()
                .fare(Fare.builder()
                        .outbound(outbound)
                        .inbound(inbound)
                        .build())
                .services(services)
                .totalAmount(totalAmount)
                .paidAmount(totalAmount)
                .build();
    }

    private String toCabinClassApiValue(String cabinClassCode, String cabinClassName) {
        if (cabinClassCode == null) {
            return cabinClassName;
        }
        CabinClass cabinClass = CabinClass.fromCode(cabinClassCode);
        if (cabinClass == null) {
            return (cabinClassName != null) ? cabinClassName : cabinClassCode;
        }
        return cabinClass.toApiValue();
    }
}
