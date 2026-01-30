package com.flyway.passenger.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyway.passenger.dto.BaggageSaveRequest;
import com.flyway.passenger.dto.PassengerServiceInsertDTO;
import com.flyway.passenger.dto.PassengerServiceView;
import com.flyway.passenger.dto.PassengerView;
import com.flyway.reservation.dto.*;
import com.flyway.passenger.repository.PassengerServiceRepository;
import com.flyway.reservation.repository.ReservationBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class PassengerServiceService {

    private final PassengerServiceRepository serviceRepository;
    private final ReservationBookingRepository bookingRepository;
    private final ObjectMapper objectMapper = new  ObjectMapper();

    @Transactional(readOnly = true)
    public ServicePopupViewModel getServicePopup(String reservationId, String userId) {

        // 예약 헤더 조회 소유자 검증
        BookingViewModel header = bookingRepository.findReservationHeaderByUser(reservationId, userId);
        if (header == null) {
            throw new IllegalArgumentException("예약자가 아닙니다.:" + reservationId);
        }

        // 예약자 저장 확인
        int savedCount = bookingRepository.countPassengers(reservationId);
        if (savedCount != header.getPassengerCount()) {
            throw new IllegalStateException("탑승자가 모두 저장되지 않았습니다");
        }

        // segments 조회
        List<ReservationSegmentView> segmentViews = bookingRepository.findSegments(reservationId);
        if (segmentViews == null || segmentViews.isEmpty()) {
            throw new IllegalStateException("확인되지 않았습니다");
        }

        // segment 응답용 데이터 전환, routeType 조회
        List<ServicePopupViewModel.SegmentServiceInfo> segments = new ArrayList<>();
        String firstCabinClass = null;
        String firstRouteType = null;

        for (ReservationSegmentView sv : segmentViews) {
            String routeType = serviceRepository.findRouteTypeBySegment(sv.getReservationSegmentId());

            ServicePopupViewModel.SegmentServiceInfo info = ServicePopupViewModel.SegmentServiceInfo.builder()
                    .reservationSegmentId(sv.getReservationSegmentId())
                    .segmentOrder(sv.getSegmentOrder())
                    .snapDepartureAirport(sv.getSnapDepartureAirport())
                    .snapArrivalAirport(sv.getSnapArrivalAirport())
                    .snapDepartureTime(sv.getSnapDepartureTime())
                    .snapArrivalTime(sv.getSnapArrivalTime())
                    .snapFlightNumber(sv.getSnapFlightNumber())
                    .routeType(routeType)
                    .build();
            segments.add(info);

            if (firstCabinClass == null) {
                firstCabinClass = sv.getSnapCabinClassCode();
                firstRouteType = routeType;
            }

        }

        // 수하물 정책
        BaggagePolicyView baggagePolicy = serviceRepository.findBaggagePolicy(firstCabinClass, firstRouteType);

        // 기내식 옵션
        boolean mealAvailable = "INTERNATIONAL".equals(firstRouteType);
        List<MealOptionView> mealOptions = mealAvailable
                ? serviceRepository.findMealOptions(firstRouteType, firstCabinClass)
                : Collections.emptyList();

        // 탑승자 목록 , 선택 서비스
        List<PassengerView> passengerViews = bookingRepository.findPassengers(reservationId);
        List<ServicePopupViewModel.PassengerServiceInfo> passengers = new ArrayList<>();

        for (PassengerView pv : passengerViews) {
            List<PassengerServiceView> baggageService = new ArrayList<>();
            List<PassengerServiceView> mealService = new ArrayList<>();

            for (ServicePopupViewModel.SegmentServiceInfo seg : segments) {
                List<PassengerServiceView> services = serviceRepository.findByPassengerAndSegment(
                        pv.getPassengerId(), seg.getReservationSegmentId()
                );
                for (PassengerServiceView svc : services) {
                    if ("0".equals(svc.getServiceType())) {
                        baggageService.add(svc);
                    } else if ("1".equals(svc.getServiceType())) {
                        mealService.add(svc);
                    }
                }
            }
            ServicePopupViewModel.PassengerServiceInfo psi = ServicePopupViewModel.PassengerServiceInfo.builder()
                    .passengerId(pv.getPassengerId())
                    .krFirstName(pv.getKrFirstName())
                    .krLastName(pv.getKrLastName())
                    .firstName(pv.getFirstName())
                    .lastName(pv.getLastName())
                    .baggageServices(baggageService)
                    .mealServices(mealService)
                    .build();
            passengers.add(psi);
        }

        return ServicePopupViewModel.builder()
                .reservationId(reservationId)
                .tripType(header.getTripType())
                .segments(segments)
                .passengers(passengers)
                .baggagePolicy(baggagePolicy)
                .mealOptions(mealOptions)
                .mealAvailable(mealAvailable)
                .build();

    }

    @Transactional
    public void saveBaggage(String reservationId, String userId, BaggageSaveRequest request) {
        BookingViewModel header = bookingRepository.findReservationHeaderByUser(reservationId, userId);
        if (header == null) {
            throw new IllegalArgumentException("확인되지 않은 예약 번호 입니다." + reservationId);
        }
        if (header.getExpiredAt() != null && header.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("no Segment found");
        }
        List<ReservationSegmentView> segments = bookingRepository.findSegments(reservationId);
        if (segments == null || segments.isEmpty()) {
            throw new IllegalStateException("no segments found");
        }
        String cabinClass = segments.get(0).getSnapCabinClassCode();
        String routeType = serviceRepository.findRouteTypeBySegment(segments.get(0).getReservationSegmentId());
        BaggagePolicyView policy = serviceRepository.findBaggagePolicy(cabinClass, routeType);

        if (policy == null) {
            throw new IllegalStateException("baggage policy not found");
        }

        // 3. 각 item 처리
        for (BaggageSaveRequest.PassengerBaggage item : request.getItems()) {

            // 기존 수하물 서비스 삭제
            serviceRepository.deleteByPassengerSegmentType(
                    item.getPassengerId(),
                    item.getReservationSegmentId(),
                    "0"
            );

            // 추가 선택이 없으면 INSERT 안함
            if (item.getExtraWeightKg() == 0 && item.getExtraBagCount() == 0) {
                continue;
            }

            // 가격 계산
            long overweightFee = (long) item.getExtraWeightKg() * policy.getOverweightFeePerKg();
            long extraBagFee = (long) item.getExtraBagCount() * policy.getExtraBagFee();
            long totalPrice = overweightFee + extraBagFee;

            // ObjectMapper로 JSON 생성
            BaggageServiceDetails detailsObj = BaggageServiceDetails.builder()
                    .extraKg(item.getExtraWeightKg())
                    .extraBags(item.getExtraBagCount())
                    .overweightFee(overweightFee)
                    .extraBagFee(extraBagFee)
                    .build();

            String details;
            try {
                details = objectMapper.writeValueAsString(detailsObj);
            } catch (Exception e) {
                log.warn("Failed to serialize baggage details", e);
                details = "{}";
            }

            PassengerServiceInsertDTO dto = PassengerServiceInsertDTO.builder()
                    .psId(UUID.randomUUID().toString())
                    .passengerId(item.getPassengerId())
                    .reservationSegmentId(item.getReservationSegmentId())
                    .mealId(null)
                    .policyId(policy.getPolicyId())
                    .serviceType("0")
                    .quantity(1)
                    .totalPrice(totalPrice)
                    .serviceDetails(details)
                    .tripType(header.getTripType())
                    .build();

            serviceRepository.insertPassengerService(dto);
        }
    }

    /**
     * 기내식 저장
     */
    @Transactional
    public void saveMeal(String reservationId, String userId, MealSaveRequest request) {

        // 1. 예약 검증 (락 없이 조회)
        BookingViewModel header = bookingRepository.findReservationHeaderByUser(reservationId, userId);
        if (header == null) {
            throw new IllegalArgumentException("reservation not found or not yours: " + reservationId);
        }
        if (header.getExpiredAt() != null && header.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("reservation expired");
        }

        // 2. 국제선 확인
        List<ReservationSegmentView> segments = bookingRepository.findSegments(reservationId);
        if (segments == null || segments.isEmpty()) {
            throw new IllegalStateException("no segments found");
        }
        String routeType = serviceRepository.findRouteTypeBySegment(segments.get(0).getReservationSegmentId());
        if (!"INTERNATIONAL".equals(routeType)) {
            throw new IllegalStateException("meal service is only available for international flights");
        }

        // 3. 각 item 처리
        for (MealSaveRequest.PassengerMeal item : request.getItems()) {

            // 기존 기내식 서비스 삭제
            serviceRepository.deleteByPassengerSegmentType(
                    item.getPassengerId(),
                    item.getReservationSegmentId(),
                    "1"
            );

            String mealId = item.getMealId();

            // 선택 안했으면 INSERT 안함
            if (mealId == null || mealId.trim().isEmpty()) {
                continue;
            }

            PassengerServiceInsertDTO dto = PassengerServiceInsertDTO.builder()
                    .psId(UUID.randomUUID().toString())
                    .passengerId(item.getPassengerId())
                    .reservationSegmentId(item.getReservationSegmentId())
                    .mealId(mealId)
                    .policyId(null)  // 기내식은 policy 불필요
                    .serviceType("1")
                    .quantity(1)
                    .totalPrice(0L)  // 무료
                    .serviceDetails(null)
                    .tripType(header.getTripType())
                    .build();

            serviceRepository.insertPassengerService(dto);
        }
    }

    /**
     * 부가서비스 총액 조회
     */
    @Transactional(readOnly = true)
    public Long getServiceTotal(String reservationId, String userId) {
        BookingViewModel header = bookingRepository.findReservationHeaderByUser(reservationId, userId);
        if (header == null) {
            throw new IllegalArgumentException("reservation not found or not yours");
        }
        return serviceRepository.findServiceTotal(reservationId);
    }
}


/**
 기능
 1. 팝업에 보여줄 데이터
 2. 수하물 옵션 선택 저장
 3. 기내식 옵션 선택 저장
 4. 부가서비스 합계 금액 조회
 ==================================

 1.팝업에 보여줄 데이터
    예약 정보 o
    구간 (편도 / 가는편,오는편) o
    탑승자 목록
    수하물 정책 o
    기내식 메뉴 (국제선) o
    기존 선택 서비스 ( 다시 들어온 경우)

 2. 수하물 옵션 선택 저장
    예약 검증
    구간 좌석 등급
    routeType
    수하물 정책
    기존 수하물 삭제 (delete)
    새 수하물 저장 (insert)

 3. 기내식 옵션 선택 저장
    예약 검증
    구간 좌석 등급
    rotueType
    기존 기내식 삭제 (delete)
    새 기내식 저장 (insert)


 4. 부가서비스 합계 금액 조회
    예약 검증
    SUM
 */