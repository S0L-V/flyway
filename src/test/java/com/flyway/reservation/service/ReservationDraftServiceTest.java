package com.flyway.reservation.service;

import com.flyway.reservation.domain.DraftCreateRequest;
import com.flyway.reservation.domain.FlightSnapshot;
import com.flyway.reservation.dto.ReservationInsertDTO;
import com.flyway.reservation.dto.ReservationSegmentInsertDTO;
import com.flyway.reservation.repository.ReservationDraftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ 단위 테스트(Unit Test)
 * - DB 없이 Service 로직만 검증
 * - Repository는 Mock 처리
 */
@ExtendWith(MockitoExtension.class)
class ReservationDraftServiceTest {

    @Mock
    private ReservationDraftRepository draftRepository;

    @InjectMocks
    private ReservationDraftService service;

    // --------------------------
    // 1) 입력 검증(예외) 케이스
    // --------------------------

    @Test
    void createDraft_outFlightId_isNull_throw() {
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId(null)
                .inFlightId(null)
                .passengerCount(1)
                .cabinClassCode("찬혁")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.createDraft("U001", req));

        verifyNoInteractions(draftRepository);
    }

    @Test
    void createDraft_outFlightId_isBlank_throw() {
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId(" ")
                .inFlightId(null)
                .passengerCount(1)
                .cabinClassCode("ECO")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.createDraft("U001", req));

        verifyNoInteractions(draftRepository);
    }

    @Test
    void createDraft_passengerCount_zero_throw() {
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId("F_OUT")
                .inFlightId(null)
                .passengerCount(0)
                .cabinClassCode("ECO")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.createDraft("U001", req));

        verifyNoInteractions(draftRepository);
    }

    @Test
    void createDraft_cabinClassCode_blank_throw() {
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId("F_OUT")
                .inFlightId(null)
                .passengerCount(1)
                .cabinClassCode(" ")
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> service.createDraft("U001", req));

        verifyNoInteractions(draftRepository);
    }

    // --------------------------
    // 2) 편도(OW) 정상 케이스
    // --------------------------

    @Test
    void createDraft_oneWay_success_insertReservation_and_oneSegment() {
        // given
        String userId = "U001";
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId("F_OUT")
                .inFlightId(null)
                .passengerCount(2)
                .cabinClassCode("ECO")
                .build();

        FlightSnapshot outSnap = FlightSnapshot.builder()
                .flightId("F_OUT")
                .departureAirport("GMP")
                .arrivalAirport("CJU")
                .departureTime(LocalDateTime.of(2026, 1, 1, 10, 0))
                .arrivalTime(LocalDateTime.of(2026, 1, 1, 11, 0))
                .flightNumber("ZE101")
                .build();

        when(draftRepository.findFlightSnapshot("F_OUT")).thenReturn(outSnap);

        // when
        String reservationId = service.createDraft(userId, req);

        // then: 결과
        assertNotNull(reservationId);
        assertFalse(reservationId.isBlank());

        // then: 호출 횟수
        verify(draftRepository, times(1)).saveReservation(any(ReservationInsertDTO.class));
        verify(draftRepository, times(1)).findFlightSnapshot("F_OUT");
        verify(draftRepository, times(1)).saveReservationSegment(any(ReservationSegmentInsertDTO.class));

        // then: reservation 저장값 검증
        ArgumentCaptor<ReservationInsertDTO> resCaptor = ArgumentCaptor.forClass(ReservationInsertDTO.class);
        verify(draftRepository).saveReservation(resCaptor.capture());

        ReservationInsertDTO savedRes = resCaptor.getValue();
        assertEquals(userId, savedRes.getUserId());
        assertEquals("HELD", savedRes.getStatus());
        assertEquals(2, savedRes.getPassengerCount());
        assertEquals("0", savedRes.getTripType()); // ✅ 편도 OW

        assertNotNull(savedRes.getReservationId());
        assertNotNull(savedRes.getReservedAt());
        assertNotNull(savedRes.getExpiredAt());
        assertTrue(savedRes.getExpiredAt().isAfter(savedRes.getReservedAt())); // ✅ now + 10분

        // then: segment 저장값 검증
        ArgumentCaptor<ReservationSegmentInsertDTO> segCaptor = ArgumentCaptor.forClass(ReservationSegmentInsertDTO.class);
        verify(draftRepository).saveReservationSegment(segCaptor.capture());

        ReservationSegmentInsertDTO seg = segCaptor.getValue();
        assertEquals(savedRes.getReservationId(), seg.getReservationId()); // ✅ reservationId 연결
        assertEquals("F_OUT", seg.getFlightId());
        assertEquals(1, seg.getSegmentOrder()); // ✅ out는 1

        // ✅ 스냅샷 복사 확인
        assertEquals("GMP", seg.getSnapDepartureAirport());
        assertEquals("CJU", seg.getSnapArrivalAirport());
        assertEquals(LocalDateTime.of(2026, 1, 1, 10, 0), seg.getSnapDepartureTime());
        assertEquals(LocalDateTime.of(2026, 1, 1, 11, 0), seg.getSnapArrivalTime());
        assertEquals("ZE101", seg.getSnapFlightNumber());

        // ✅ 선택한 등급 스냅샷
        assertEquals("ECO", seg.getSnapCabinClassCode());
    }

    // --------------------------
    // 3) 왕복(RT) 정상 케이스
    // --------------------------

    @Test
    void createDraft_roundTrip_success_insertReservation_and_twoSegments() {
        // given
        String userId = "U001";
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId("F_OUT")
                .inFlightId("F_IN")
                .passengerCount(1)
                .cabinClassCode("BIZ")
                .build();

        FlightSnapshot outSnap = FlightSnapshot.builder()
                .flightId("F_OUT")
                .departureAirport("GMP")
                .arrivalAirport("CJU")
                .departureTime(LocalDateTime.of(2026, 1, 1, 10, 0))
                .arrivalTime(LocalDateTime.of(2026, 1, 1, 11, 0))
                .flightNumber("ZE101")
                .build();

        FlightSnapshot inSnap = FlightSnapshot.builder()
                .flightId("F_IN")
                .departureAirport("CJU")
                .arrivalAirport("GMP")
                .departureTime(LocalDateTime.of(2026, 1, 2, 10, 0))
                .arrivalTime(LocalDateTime.of(2026, 1, 2, 11, 0))
                .flightNumber("ZE102")
                .build();

        when(draftRepository.findFlightSnapshot("F_OUT")).thenReturn(outSnap);
        when(draftRepository.findFlightSnapshot("F_IN")).thenReturn(inSnap);

        // when
        String reservationId = service.createDraft(userId, req);

        // then
        assertNotNull(reservationId);

        verify(draftRepository, times(1)).saveReservation(any(ReservationInsertDTO.class));
        verify(draftRepository, times(2)).findFlightSnapshot(anyString());
        verify(draftRepository, times(2)).saveReservationSegment(any(ReservationSegmentInsertDTO.class));

        // tripType이 "1"인지 확인
        ArgumentCaptor<ReservationInsertDTO> resCaptor = ArgumentCaptor.forClass(ReservationInsertDTO.class);
        verify(draftRepository).saveReservation(resCaptor.capture());
        assertEquals("1", resCaptor.getValue().getTripType()); // ✅ 왕복 RT

        // segment 2개(order 1,2) 확인
        ArgumentCaptor<ReservationSegmentInsertDTO> segCaptor = ArgumentCaptor.forClass(ReservationSegmentInsertDTO.class);
        verify(draftRepository, times(2)).saveReservationSegment(segCaptor.capture());

        List<ReservationSegmentInsertDTO> segs = segCaptor.getAllValues();
        ReservationSegmentInsertDTO seg1 = segs.get(0);
        ReservationSegmentInsertDTO seg2 = segs.get(1);

        assertEquals(1, seg1.getSegmentOrder());
        assertEquals("F_OUT", seg1.getFlightId());
        assertEquals("BIZ", seg1.getSnapCabinClassCode());

        assertEquals(2, seg2.getSegmentOrder());
        assertEquals("F_IN", seg2.getFlightId());
        assertEquals("BIZ", seg2.getSnapCabinClassCode());
    }

    // --------------------------
    // 4) 스냅샷 조회 실패(예외) 케이스
    // --------------------------

    @Test
    void createDraft_outFlightSnapshot_null_throw() {
        // given
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId("F_OUT")
                .inFlightId(null)
                .passengerCount(1)
                .cabinClassCode("ECO")
                .build();

        when(draftRepository.findFlightSnapshot("F_OUT")).thenReturn(null);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> service.createDraft("U001", req));

        // ✅ 네 서비스 코드 흐름상 reservation은 먼저 저장 시도함
        verify(draftRepository, times(1)).saveReservation(any());
        verify(draftRepository, times(1)).findFlightSnapshot("F_OUT");
        verify(draftRepository, never()).saveReservationSegment(any());
    }

    @Test
    void createDraft_inFlightSnapshot_null_throw() {
        // given
        DraftCreateRequest req = DraftCreateRequest.builder()
                .outFlightId("F_OUT")
                .inFlightId("F_IN")
                .passengerCount(1)
                .cabinClassCode("ECO")
                .build();

        FlightSnapshot outSnap = FlightSnapshot.builder()
                .flightId("F_OUT")
                .departureAirport("GMP")
                .arrivalAirport("CJU")
                .departureTime(LocalDateTime.of(2026, 1, 1, 10, 0))
                .arrivalTime(LocalDateTime.of(2026, 1, 1, 11, 0))
                .flightNumber("ZE101")
                .build();

        when(draftRepository.findFlightSnapshot("F_OUT")).thenReturn(outSnap);
        when(draftRepository.findFlightSnapshot("F_IN")).thenReturn(null);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> service.createDraft("U001", req));

        // out은 저장됨, in에서 터짐(네 코드 흐름)
        verify(draftRepository, times(1)).saveReservation(any());
        verify(draftRepository, times(2)).findFlightSnapshot(anyString());
        verify(draftRepository, times(1)).saveReservationSegment(any()); // out segment 1개까지만
    }
}
