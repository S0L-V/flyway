package com.flyway.sender.service;

import com.flyway.sender.config.SmsConfig;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class SmsService {

    private final SmsConfig smsConfig;
    private DefaultMessageService messageService;

    public SmsService(SmsConfig smsConfig) {
        this.smsConfig = smsConfig;
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
}