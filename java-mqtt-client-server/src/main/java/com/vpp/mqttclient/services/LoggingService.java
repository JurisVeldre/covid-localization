package com.vpp.mqttclient.services;

import com.vpp.mqttclient.domains.Log;
import com.vpp.mqttclient.repositories.LogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class LoggingService implements NotificationServiceInterface {

    @Autowired
    private LogRepository logRepository;

    @Override
    public void logError(String node_id, String msgType, String payload, Exception ex) {
        try {
            log.info("Logging error..");

            Date date = new Date(System.currentTimeMillis());

            Log logEntity = new Log();
            logEntity.setPayload(payload);
            logEntity.setMessageType(msgType);
            logEntity.setErrorMessage(ex.getMessage());
            logEntity.setNode_id(node_id);
            logEntity.setTimestamp(date);


            logRepository.save(logEntity);

            log.info("Error logged");
        } catch (Exception exception) {
            log.error("Error sending the message.");
            log.error("Error description: ", ex);
        }
    }
}
