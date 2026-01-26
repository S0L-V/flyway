package com.flyway.reservation.mapper;


import com.flyway.reservation.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PassengerServiceMapper {

    // 수하물 정책 조회
    BaggagePolicyView selectBaggagePolicy(
            @Param("cabinClassCode") String cabinClassCode,
            @Param("routeType") String routeType
    );

    // 기내식 옵션 조회
    List<MealOptionView> selectMealOptions(
            @Param("routeType") String routeType,
            @Param("cabinClassCode") String cabinClassCode
    );

    // route_type (flight 테이블 조인)
    String selectRouteTypeBySegment(@Param("reservationSegmentId") String reservationSegmentId);

    // 탑승자+구간별 부가서비스 조회
    List<PassengerServiceView> selectByPassengerAndSegment(
            @Param("passengerId") String passengerId,
            @Param("reservationSegmentId") String reservationSegmentId
    );

    // 전체 부가서비스 총액
    Long selectServiceTotalByReservation(@Param("reservationId") String reservationId);

    // 부가서비스 insert
    int insertPassengerService(PassengerServiceInsertDTO dto);

    // 탑승자+구간+타입별 기존 서비스 삭제
    int deleteByPassengerSegmentType(
            @Param("passengerId") String passengerId,
            @Param("reservationSegmentId") String reservationSegmentId,
            @Param("serviceType") String serviceType
    );
}