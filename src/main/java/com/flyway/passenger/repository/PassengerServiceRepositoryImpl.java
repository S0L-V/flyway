package com.flyway.passenger.repository;

import com.flyway.reservation.dto.BaggagePolicyView;
import com.flyway.passenger.dto.PassengerServiceInsertDTO;
import com.flyway.passenger.dto.PassengerServiceView;
import com.flyway.passenger.mapper.PassengerServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import com.flyway.reservation.dto.MealOptionView;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PassengerServiceRepositoryImpl implements PassengerServiceRepository {

    private final PassengerServiceMapper serviceMapper;

    @Override
    public BaggagePolicyView findBaggagePolicy(String cabinClassCode, String routeType) {
        return serviceMapper.selectBaggagePolicy(cabinClassCode, routeType);
    }

    @Override
    public List<MealOptionView> findMealOptions(String routeType, String cabinClassCode) {
        return serviceMapper.selectMealOptions(routeType, cabinClassCode);
    }

    @Override
    public String findRouteTypeBySegment(String reservationSegmentId) {
        return serviceMapper.selectRouteTypeBySegment(reservationSegmentId);
    }

    @Override
    public List<PassengerServiceView> findByPassengerAndSegment(String passengerId, String reservationSegmentId) {
        return serviceMapper.selectByPassengerAndSegment(passengerId, reservationSegmentId);
    }

    @Override
    public Long findServiceTotal(String reservationId) {
        Long total = serviceMapper.selectServiceTotalByReservation(reservationId);
        return (total == null) ? 0L : total;
    }

    @Override
    public int insertPassengerService(PassengerServiceInsertDTO dto) {
        return serviceMapper.insertPassengerService(dto);
    }

    @Override
    public int deleteByPassengerSegmentType(String passengerId, String reservationSegmentId, String serviceType) {
        return serviceMapper.deleteByPassengerSegmentType(passengerId, reservationSegmentId, serviceType);
    }
}