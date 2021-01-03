package com.vpp.mqttclient.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Component
@Slf4j
public class NotificationService implements NotificationServiceInterface{
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${dwm.email-recipients}")
    private String recipients;

    @Override
    public void logError(String node_id, String msgType, String payload, Exception ex) {
        try {
            log.info("Preparing message..");

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setSubject("DWM ERROR NOTIFICATION");
            String message = ex.getMessage() + "\n" + "Node Id = " + node_id + "; Message type = " + msgType + "\n" + "Message: " + payload + "\n\n" + Arrays.toString(ex.getStackTrace());
            msg.setText(message);

            String[] recipientsSplit = recipients.split(",");
            for (String recipient : recipientsSplit) {
                msg.setTo(recipient);
            }

            javaMailSender.send(msg);

            log.info("Error message sent");
        } catch (Exception exception) {
            log.error("Error sending the message.");
            log.error("Error description: ", ex);
        }
    }
}
