package com.flyway.sender.service;

import com.flyway.reservation.dto.ReservationSegmentView;
import com.flyway.reservation.repository.ReservationBookingRepository;
import com.flyway.sender.config.SmsConfig;
import com.flyway.sender.dto.PassengerSmsInfo;
import com.flyway.sender.dto.PassengerTicketInfo;
import com.flyway.sender.mapper.SmsMapper;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmsService {

    private final SmsConfig smsConfig;
    private final SmsMapper smsMapper;
    private final ReservationBookingRepository reservationBookingRepository;
    private DefaultMessageService messageService;

    public SmsService(SmsConfig smsConfig,
                      SmsMapper smsMapper,
                      ReservationBookingRepository reservationBookingRepository) {
        this.smsConfig = smsConfig;
        this.smsMapper = smsMapper;
        this.reservationBookingRepository = reservationBookingRepository;
    }

    @PostConstruct
    public void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(
                smsConfig.getApiKey(),
                smsConfig.getApiSecret(),
                "https://api.coolsms.co.kr"
        );
    }

    public void sendSms(String to, String content) {
        try {
            Message message = new Message();
            message.setFrom(smsConfig.getSender());
            message.setTo(to);
            message.setText(content);

            SingleMessageSentResponse response = messageService.sendOne(
                    new SingleMessageSendingRequest(message)
            );
            log.info("[SMS] 발송 성공 - to: {}, messageId: {}", to, response.getMessageId());
        } catch (Exception e) {
            log.error("[SMS] 발송 실패 - to: {}, error: {}", to, e.getMessage());
        }
    }

    public void sendPaymentComplete(String to, String reservationId, Long amount) {
        String content = String.format(
                "[Flyway] 결제가 완료되었습니다.\n예약번호: %s\n결제금액: %,d원",
                reservationId, amount
        );
        sendSms(to, content);
    }

    public void sendRefundComplete(String to, String reservationId, Long amount) {
        String content = String.format(
                "[Flyway] 환불이 완료되었습니다.\n예약번호: %s\n환불금액: %,d원",
                reservationId, amount
        );
        sendSms(to, content);
    }

    // 탑승객 티켓 정보를 예약자(회원) 번호로 SMS 발송
    public void sendTicketInfoToPassengers(String reservationId) {

        // 1. 예약자(회원)의 전화번호 조회
        String userPhone = smsMapper.selectPhoneByReservationId(reservationId);
        if (userPhone == null || userPhone.isEmpty()) {
            log.info("[SMS] 티켓 발송 실패 - 회원 전화번호 없음, reservationId: {}", reservationId);
            return;
        }

        // 2. 항공편 정보 조회
        List<ReservationSegmentView> segments =
                reservationBookingRepository.findSegments(reservationId);

        // 3. 탑승객별 티켓 정보 조회 (좌석/기내식/수하물 포함)
        List<PassengerTicketInfo> ticketInfoList =
                smsMapper.selectPassengerTicketInfo(reservationId);

        if (ticketInfoList == null || ticketInfoList.isEmpty()) {
            log.info("[SMS] 티켓 발송 대상 없음 - reservationId: {}", reservationId);
            return;
        }

        // 4. passengerId별로 그룹핑
        Map<String, List<PassengerTicketInfo>> passengerMap = ticketInfoList.stream()
                .collect(Collectors.groupingBy(PassengerTicketInfo::getPassengerId));

        // 5. 각 탑승객 정보를 회원 번호로 발송
        for (Map.Entry<String, List<PassengerTicketInfo>> entry : passengerMap.entrySet()) {
            List<PassengerTicketInfo> paxInfoList = entry.getValue();
            PassengerTicketInfo firstInfo = paxInfoList.get(0);

            try {
                String content = buildTicketSmsContent(firstInfo, segments, paxInfoList);
                sendSms(userPhone, content);  // 회원 번호로 발송
                log.info("[SMS] 티켓 발송 - passenger: {} {}, to userPhone: {}",
                        firstInfo.getFirstName(), firstInfo.getLastName(), userPhone);
            } catch (Exception e) {
                log.error("[SMS] 티켓 발송 실패 - passengerId: {}, error: {}",
                        firstInfo.getPassengerId(), e.getMessage());
            }
        }
    }

    /**
     * 티켓 SMS 내용 생성
     */
    private String buildTicketSmsContent(PassengerTicketInfo pax,
                                         List<ReservationSegmentView> segments,
                                         List<PassengerTicketInfo> paxInfoList) {

        StringBuilder sb = new StringBuilder();
        sb.append("[Flyway 항공권]\n");
        sb.append("탑승객: ").append(pax.getFirstName()).append(" ").append(pax.getLastName()).append("\n\n");

        for (ReservationSegmentView seg : segments) {
            sb.append("【구간 ").append(seg.getSegmentOrder()).append("】\n");
            sb.append(seg.getSnapFlightNumber()).append("\n");
            sb.append(seg.getSnapDepartureAirport()).append(" → ").append(seg.getSnapArrivalAirport()).append("\n");
            sb.append(formatDateTime(seg.getSnapDepartureTime())).append("\n");

            // 해당 구간의 모든 정보 가져오기
            List<PassengerTicketInfo> segmentInfoList = paxInfoList.stream()
                    .filter(info -> seg.getReservationSegmentId().equals(info.getReservationSegmentId()))
                    .collect(Collectors.toList());

            // 좌석 (아무 row에서나 - 같은 값)
            segmentInfoList.stream().findFirst().ifPresent(info -> {
                if (info.getSeatNo() != null && !info.getSeatNo().isEmpty()) {
                    sb.append("좌석: ").append(info.getSeatNo()).append("\n");
                }
            });

            // 기내식 (service_type = '1')
            segmentInfoList.stream()
                    .filter(info -> "1".equals(info.getServiceType()))
                    .findFirst()
                    .ifPresent(info -> {
                        if (info.getMealName() != null && !info.getMealName().isEmpty()) {
                            sb.append("기내식: ").append(info.getMealName()).append("\n");
                        }
                    });

            // 수하물 (service_type = '0')
            segmentInfoList.stream()
                    .filter(info -> "0".equals(info.getServiceType()))
                    .findFirst()
                    .ifPresent(info -> {
                        if (info.getServiceDetails() != null && !info.getServiceDetails().isEmpty()) {
                            String baggageInfo = parseBaggageInfo(info.getServiceDetails());
                            if (!baggageInfo.isEmpty()) {
                                sb.append("수하물: ").append(baggageInfo).append("\n");
                            }
                        }
                    });

            sb.append("\n");
        }

        return sb.toString();
    }
    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String parseBaggageInfo(String serviceDetails) {
        try {
            StringBuilder result = new StringBuilder();

            if (serviceDetails.contains("extraKg")) {
                int kgStart = serviceDetails.indexOf("extraKg") + 9;
                int kgEnd = serviceDetails.indexOf(",", kgStart);
                if (kgEnd == -1) kgEnd = serviceDetails.indexOf("}", kgStart);
                String kgStr = serviceDetails.substring(kgStart, kgEnd).trim();
                int kg = Integer.parseInt(kgStr);
                if (kg > 0) {
                    result.append("+").append(kg).append("kg");
                }
            }

            if (serviceDetails.contains("extraBags")) {
                int bagStart = serviceDetails.indexOf("extraBags") + 11;
                int bagEnd = serviceDetails.indexOf("}", bagStart);
                String bagStr = serviceDetails.substring(bagStart, bagEnd).trim();
                int bags = Integer.parseInt(bagStr);
                if (bags > 0) {
                    if (result.length() > 0) result.append(", ");
                    result.append("추가 ").append(bags).append("개");
                }
            }

            return result.toString();
        } catch (Exception e) {
            log.warn("[SMS] 수하물 정보 파싱 실패: {}", serviceDetails);
            return "";
        }
    }
}