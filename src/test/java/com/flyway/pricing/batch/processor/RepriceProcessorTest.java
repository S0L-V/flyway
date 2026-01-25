package com.flyway.pricing.batch.processor;

import com.flyway.pricing.service.DynamicPricingCalculator;
import com.flyway.pricing.batch.row.PricingResultRow;
import com.flyway.pricing.batch.row.RepriceCandidateRow;
import com.flyway.pricing.model.PricingInput;
import com.flyway.pricing.model.PricingResult;
import com.flyway.pricing.service.SeatAvailabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class RepriceProcessorTest {

    @Mock
    DynamicPricingCalculator calculator;

    @Mock
    SeatAvailabilityServiceImpl seatService;

    @InjectMocks
    RepriceProcessor processor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // StepScope @Value 주입 대체
        ReflectionTestUtils.setField(
                processor,
                "asOfEpochMillis",
                1700000000000L // 임의 epoch millis
        );
    }

    @Test
    void 가격미적용_결과는_null() throws Exception {
        // given
        RepriceCandidateRow item = sampleCandidate();

        Mockito.when(seatService.getSeatStatus(any(), any()))
                .thenReturn(new SeatAvailabilityServiceImpl.SeatStatus(180, 120));

        Mockito.when(calculator.calculate(any(PricingInput.class)))
                .thenReturn(
                        PricingResult.builder()
                                .applied(false)
                                .build()
                );

        // when
        PricingResultRow result = processor.process(item);

        // then
        assertThat(result).isNull();
    }

    @Test
    void 가격적용_결과Row생성() throws Exception {
        // given
        RepriceCandidateRow item = sampleCandidate();

        Mockito.when(seatService.getSeatStatus(any(), any()))
                .thenReturn(new SeatAvailabilityServiceImpl.SeatStatus(180, 120));

        Mockito.when(calculator.calculate(any(PricingInput.class)))
                .thenReturn(
                        PricingResult.builder()
                                .applied(true)
                                .newPrice(155_000L)
                                .build()
                );

        // when
        PricingResultRow result = processor.process(item);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFlightId()).isEqualTo(item.getFlightId());
        assertThat(result.getNewPrice()).isEqualTo(155_000L);
        assertThat(result.isApplied()).isTrue();
        assertThat(result.getCalculatedAt()).isNotNull();
    }

    private RepriceCandidateRow sampleCandidate() {
        return RepriceCandidateRow.builder()
                .flightId("FLIGHT-001")
                .cabinClassCode("ECO")
                .basePrice(120_000L)
                .currentPrice(150_000L)
                .departureTime(LocalDateTime.now().plusDays(3))
                .lastEventPricedAt(null)
                .build();
    }
}
