package com.flyway.passenger.repository;

import com.flyway.reservation.dto.BaggagePolicyView;
import com.flyway.reservation.dto.MealOptionView;
import com.flyway.passenger.dto.PassengerServiceInsertDTO;
import com.flyway.passenger.dto.PassengerServiceView;

import java.util.List;

public interface PassengerServiceRepository {

    // 1. 수하물 정책 조회
    BaggagePolicyView findBaggagePolicy(String cabinClassCode, String routeType);

    // 2. 기내식 메뉴 조회
    List<MealOptionView> findMealOptions(String routeType, String cabinClassCode);

    // 3. route_type 조회
    String findRouteTypeBySegment(String reservationSegmentId);

    // 4. 기존 서비스 조회
    List<PassengerServiceView> findByPassengerAndSegment(String passengerId, String reservationSegmentId);

    // 5. 기존 서비스 삭제 ( update 보다 삭제 후 새로 저장)
    int deleteByPassengerSegmentType(String passengerId, String reservationSegmentId, String serviceType);

    // 6. 새 서비스 저장
    int insertPassengerService(PassengerServiceInsertDTO dto);

    // 7. 합계 금액 조회
    Long findServiceTotal(String reservationId);
}
