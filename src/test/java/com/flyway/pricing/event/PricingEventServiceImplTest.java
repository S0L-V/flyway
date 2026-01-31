package com.flyway.pricing.event;

import com.flyway.payment.mapper.RefundMapper;
import com.flyway.pricing.mapper.FlightSeatPriceMapper;
import com.flyway.pricing.mapper.PriceHistoryMapper;
import com.flyway.pricing.mapper.PricingEventMapper;
import com.flyway.pricing.model.EventRepriceSegment;
import com.flyway.pricing.model.FlightSeatPriceRow;
import com.flyway.pricing.model.PricingInput;
import com.flyway.pricing.model.PricingResult;
import com.flyway.pricing.policy.PricingPolicy;
import com.flyway.pricing.policy.PricingSkipReason;
import com.flyway.pricing.service.DynamicPricingCalculator;
import com.flyway.reservation.repository.ReservationBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingEventServiceImplTest {

    @InjectMocks
    private PricingEventServiceImpl pricingEventService;

    // === dependencies to mock ===
    @Mock private PricingEventMapper pricingEventMapper;
    @Mock private ReservationBookingRepository reservationBookingRepository; // segments 조회
    @Mock private RefundMapper refundMapper; // passengerCount 조회
    @Mock private FlightSeatPriceMapper flightSeatPriceMapper;
    @Mock private PriceHistoryMapper priceHistoryMapper;
    @Mock private DynamicPricingCalculator pricingCalculator;
    @Mock private PricingPolicy pricingPolicy;

    // 테스트 고정 시간
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2026, 2, 1, 12, 0, 0);
    }

    @Test
    void payment_event_should_write_policy_version_and_apply_price() {
        // given
        when(pricingPolicy.version()).thenReturn("v1");

        String reservationId = "R1";
        String triggerId = "P1";

        // 세그먼트 목록 stubbing
        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(LocalDateTime.now().plusDays(10))
                .build();
        when(pricingEventMapper.selectRepriceSegments(reservationId))
                .thenReturn(List.of(seg));

        // 좌석 row stubbing
        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(80);
        row.setDepartureTime(LocalDateTime.now().plusDays(10));
        row.setLastEventPricedAt(LocalDateTime.now().minusHours(2));
        when(flightSeatPriceMapper.selectForUpdate("F1", "ECO")).thenReturn(row);

        // calculator 결과 stubbing (applied 시그니처에 맞춰 채우기)
        when(pricingCalculator.calculate(any(PricingInput.class)))
                .thenReturn(PricingResult.applied(
                        120_000L, // targetPrice
                        120_000L, // newPrice
                        0.8,      // r
                        1.0,      // mLoad
                        1.0,      // mTime
                        0.3       // alpha
                ));

        // when
        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        // then
        verify(flightSeatPriceMapper, times(1))
                .updateAfterEventPricing(eq("F1"), eq("ECO"), eq(120_000L), any(LocalDateTime.class));
        verify(priceHistoryMapper, times(1)).insertEventHistory(any());
    }

    @Test
    void refund_event_should_set_bypassCooldown_true_in_pricingInput() {
        // given
        String reservationId = "rsv-1";
        String triggerId = "refund-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(pricingEventMapper.selectRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));

        when(refundMapper.selectPassengerCountByReservationId(eq(reservationId)))
                .thenReturn(2);

        // flight_seat_price row
        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(1)); // 쿨다운 걸릴만한 상황
        row.setDepartureTime(now.plusDays(3));
        when(flightSeatPriceMapper.selectForUpdate(eq("F1"), eq("ECO")))
                .thenReturn(row);

        // calculator 호출 시 input 캡처해서 bypassCooldown 확인
        ArgumentCaptor<PricingInput> captor = ArgumentCaptor.forClass(PricingInput.class);
        when(pricingCalculator.calculate(captor.capture()))
                .thenReturn(PricingResult.skipped(PricingSkipReason.COOLDOWN));

        // when
        pricingEventService.repriceAfterRefund(reservationId, triggerId);

        // then: Refund는 bypassCooldown=true로 들어가야 함
        PricingInput used = captor.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(used.isEventBased());
        org.junit.jupiter.api.Assertions.assertTrue(used.isBypassCooldown()); // 핵심
    }

    @Test
    void payment_event_should_set_bypassCooldown_false_in_pricingInput() {
        // given
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(pricingEventMapper.selectRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));

        when(refundMapper.selectPassengerCountByReservationId(eq(reservationId)))
                .thenReturn(1);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(30));
        row.setDepartureTime(now.plusDays(10));
        when(flightSeatPriceMapper.selectForUpdate("F1", "ECO"))
                .thenReturn(row);

        when(pricingCalculator.calculate(any(PricingInput.class)))
                .thenReturn(PricingResult.skipped(PricingSkipReason.SMALL_DIFF));

        // when
        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        // then
        ArgumentCaptor<PricingInput> captor = ArgumentCaptor.forClass(PricingInput.class);
        verify(pricingCalculator, atLeastOnce()).calculate(captor.capture());

        PricingInput input = captor.getValue();
        assertThat(input.isEventBased()).isTrue();
        assertThat(input.isBypassCooldown()).isFalse();
    }

    @Test
    void when_no_flightSeatPriceRow_then_should_skip_and_not_update_db() {
        // given
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(pricingEventMapper.selectRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));

        when(refundMapper.selectPassengerCountByReservationId(eq(reservationId)))
                .thenReturn(1);

        when(flightSeatPriceMapper.selectForUpdate("F1", "ECO"))
                .thenReturn(null);

        // when
        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        // then
        verify(pricingCalculator, never()).calculate(any());
        verify(flightSeatPriceMapper, never()).updateAfterEventPricing(anyString(), anyString(), anyLong(), any());
        verify(priceHistoryMapper, never()).insertEventHistory(any());
    }

    @Test
    void when_calculator_skips_then_should_not_update_db() {
        // given
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(pricingEventMapper.selectRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));

        when(refundMapper.selectPassengerCountByReservationId(eq(reservationId)))
                .thenReturn(1);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(1));
        row.setDepartureTime(now.plusDays(10));

        when(flightSeatPriceMapper.selectForUpdate("F1", "ECO"))
                .thenReturn(row);

        when(pricingCalculator.calculate(any(PricingInput.class)))
                .thenReturn(PricingResult.skipped(PricingSkipReason.COOLDOWN));

        // when
        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        // then
        verify(flightSeatPriceMapper, never()).updateAfterEventPricing(anyString(), anyString(), anyLong(), any());
        verify(priceHistoryMapper, never()).insertEventHistory(any());
    }

    @Test
    void when_calculator_applies_then_should_update_price_and_insert_history() {
        // given
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        when(pricingPolicy.version()).thenReturn("v1");

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(pricingEventMapper.selectRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));

        when(refundMapper.selectPassengerCountByReservationId(eq(reservationId)))
                .thenReturn(1);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(30));
        row.setDepartureTime(now.plusDays(10));
        when(flightSeatPriceMapper.selectForUpdate("F1", "ECO"))
                .thenReturn(row);

        when(pricingCalculator.calculate(any(PricingInput.class)))
                .thenReturn(PricingResult.applied(
                        120_000L, // targetPrice
                        120_000L, // newPrice
                        0.5,      // r
                        1.10,     // mLoad
                        1.00,     // mTime
                        0.30      // alpha
                ));

        // when
        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        // then
        verify(flightSeatPriceMapper, times(1))
                .updateAfterEventPricing(eq("F1"), eq("ECO"), eq(120_000L), any(LocalDateTime.class));

        verify(priceHistoryMapper, times(1))
                .insertEventHistory(any());
    }
}
