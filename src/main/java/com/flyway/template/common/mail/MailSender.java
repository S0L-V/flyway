package com.flyway.template.common.mail;

public interface MailSender {
    void sendText(String to, String subject, String text);

    void sendHtml(String to, String subject, String html);
}
