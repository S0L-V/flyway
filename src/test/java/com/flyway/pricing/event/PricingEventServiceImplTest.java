package com.flyway.pricing.event;

import com.flyway.pricing.model.EventRepriceSegment;
import com.flyway.pricing.model.FlightSeatPriceRow;
import com.flyway.pricing.model.PricingInput;
import com.flyway.pricing.model.PricingResult;
import com.flyway.pricing.policy.PricingPolicy;
import com.flyway.pricing.policy.PricingSkipReason;
import com.flyway.pricing.repository.EventRepriceRepository;
import com.flyway.pricing.repository.FlightSeatPriceRepository;
import com.flyway.pricing.repository.PriceHistoryRepository;
import com.flyway.pricing.service.DynamicPricingCalculator;
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

    @Mock private DynamicPricingCalculator pricingCalculator;
    @Mock private PricingPolicy pricingPolicy;

    @Mock private FlightSeatPriceRepository flightSeatPriceRepository;
    @Mock private EventRepriceRepository eventRepriceRepository;
    @Mock private PriceHistoryRepository priceHistoryRepository;

    // 테스트 고정 시간(서비스 내부 now는 LocalDateTime.now()라 고정 불가. row 세팅용으로만 사용)
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.of(2026, 2, 1, 12, 0, 0);
    }

    @Test
    void when_no_segments_then_should_not_call_calculator_and_not_update_db() {
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        when(eventRepriceRepository.findRepriceSegments(eq(reservationId)))
                .thenReturn(List.of());

        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        verify(pricingCalculator, never()).calculate(any());
        verify(flightSeatPriceRepository, never()).updateAfterEventPricing(anyString(), anyString(), anyLong(), any());
        verify(priceHistoryRepository, never()).insertEventHistory(any());
    }

    @Test
    void when_no_flightSeatPriceRow_then_should_skip_and_not_update_db() {
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(eventRepriceRepository.findRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));
        when(eventRepriceRepository.selectPassengerCount(eq(reservationId)))
                .thenReturn(1);

        when(flightSeatPriceRepository.lockForUpdate(eq("F1"), eq("ECO")))
                .thenReturn(null);

        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        verify(pricingCalculator, never()).calculate(any());
        verify(flightSeatPriceRepository, never()).updateAfterEventPricing(anyString(), anyString(), anyLong(), any());
        verify(priceHistoryRepository, never()).insertEventHistory(any());
    }

    @Test
    void when_calculator_skips_then_should_not_update_db() {
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(eventRepriceRepository.findRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));
        when(eventRepriceRepository.selectPassengerCount(eq(reservationId)))
                .thenReturn(1);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(1));
        row.setDepartureTime(now.plusDays(10));

        when(flightSeatPriceRepository.lockForUpdate(eq("F1"), eq("ECO")))
                .thenReturn(row);

        when(pricingCalculator.calculate(any(PricingInput.class)))
                .thenReturn(PricingResult.skipped(PricingSkipReason.COOLDOWN));

        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        verify(flightSeatPriceRepository, never()).updateAfterEventPricing(anyString(), anyString(), anyLong(), any());
        verify(priceHistoryRepository, never()).insertEventHistory(any());
    }

    @Test
    void when_calculator_applies_then_should_update_price_and_insert_history() {
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        when(pricingPolicy.version()).thenReturn("v1");

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(eventRepriceRepository.findRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));
        when(eventRepriceRepository.selectPassengerCount(eq(reservationId)))
                .thenReturn(1);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(30));
        row.setDepartureTime(now.plusDays(10));

        when(flightSeatPriceRepository.lockForUpdate(eq("F1"), eq("ECO")))
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

        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        verify(flightSeatPriceRepository, times(1))
                .updateAfterEventPricing(eq("F1"), eq("ECO"), eq(120_000L), any(LocalDateTime.class));

        verify(priceHistoryRepository, times(1))
                .insertEventHistory(any());
    }

    @Test
    void refund_event_should_set_bypassCooldown_true_in_pricingInput() {
        String reservationId = "rsv-1";
        String triggerId = "refund-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(eventRepriceRepository.findRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));
        when(eventRepriceRepository.selectPassengerCount(eq(reservationId)))
                .thenReturn(2);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(1)); // 쿨다운 걸릴만한 상황
        row.setDepartureTime(now.plusDays(3));

        when(flightSeatPriceRepository.lockForUpdate(eq("F1"), eq("ECO")))
                .thenReturn(row);

        ArgumentCaptor<PricingInput> captor = ArgumentCaptor.forClass(PricingInput.class);
        when(pricingCalculator.calculate(captor.capture()))
                .thenReturn(PricingResult.skipped(PricingSkipReason.COOLDOWN));

        pricingEventService.repriceAfterRefund(reservationId, triggerId);

        PricingInput used = captor.getValue();
        assertThat(used.isEventBased()).isTrue();
        assertThat(used.isBypassCooldown()).isTrue(); // refund면 true
    }

    @Test
    void payment_event_should_set_bypassCooldown_false_in_pricingInput() {
        String reservationId = "rsv-1";
        String triggerId = "payment-1";

        EventRepriceSegment seg = EventRepriceSegment.builder()
                .flightId("F1")
                .segmentOrder(1)
                .cabinClassCode("ECO")
                .departureTime(now.plusDays(3))
                .build();

        when(eventRepriceRepository.findRepriceSegments(eq(reservationId)))
                .thenReturn(List.of(seg));
        when(eventRepriceRepository.selectPassengerCount(eq(reservationId)))
                .thenReturn(1);

        FlightSeatPriceRow row = new FlightSeatPriceRow();
        row.setBasePrice(100_000L);
        row.setCurrentPrice(110_000L);
        row.setTotalSeats(100);
        row.setRemainingSeats(50);
        row.setLastEventPricedAt(now.minusMinutes(30));
        row.setDepartureTime(now.plusDays(10));

        when(flightSeatPriceRepository.lockForUpdate(eq("F1"), eq("ECO")))
                .thenReturn(row);

        ArgumentCaptor<PricingInput> captor = ArgumentCaptor.forClass(PricingInput.class);
        when(pricingCalculator.calculate(captor.capture()))
                .thenReturn(PricingResult.skipped(PricingSkipReason.SMALL_DIFF));

        pricingEventService.repriceAfterPayment(reservationId, triggerId);

        PricingInput used = captor.getValue();
        assertThat(used.isEventBased()).isTrue();
        assertThat(used.isBypassCooldown()).isFalse(); // payment면 false
    }
}
