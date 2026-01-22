package com.flyway.template.common.mail;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class GmailSmtpMailSender implements MailSender {

    private static final String CHARSET = "UTF-8";
    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";

    private final JavaMailSender mailSender;

    @Value("${mail.smtp.username}")
    private String fromAddress;

    @Value("${mail.from.name:Flyway}")
    private String fromName;

    @Override
    public void sendText(String to, String subject, String text) {
        send(to, subject, text, false);
    }

    @Override
    public void sendHtml(String to, String subject, String html) {
        send(to, subject, html, true);
    }

    private void send(String to, String subject, String body, boolean html) {
        String type = html ? "html" : "text";
        try {
            MimeMessage message = buildMessage(to, subject, body, html);
            mailSender.send(message);
            log.debug("[MAIL] sent {} mail. to={}, subject={}", type, to, subject);
        } catch (Exception e) {
            log.error("[MAIL] failed to send {} mail. to={}, subject={}", type, to, subject, e);
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    private MimeMessage buildMessage(String to, String subject, String body, boolean html) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(fromAddress, fromName, CHARSET));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, CHARSET);
        if (html) {
            message.setContent(body, HTML_CONTENT_TYPE);
        } else {
            message.setText(body, CHARSET);
        }
        return message;
    }
}
