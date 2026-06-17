package com.logistics.notification.infrastructure.email;

import com.logistics.notification.domain.model.Notification;
import com.logistics.notification.domain.model.NotificationChannel;
import com.logistics.notification.domain.ports.out.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;

    public EmailNotificationSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.getRecipientAddress());
        message.setSubject(notification.getSubject());
        message.setText(notification.getBody());
        message.setFrom("noreply@smart-logistics.com");
        mailSender.send(message);
        log.info("Email sent to {} for ref={}", notification.getRecipientAddress(), notification.getReferenceId());
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }
}
