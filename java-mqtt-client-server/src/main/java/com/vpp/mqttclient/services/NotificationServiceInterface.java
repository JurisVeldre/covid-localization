package com.vpp.mqttclient.services;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public interface NotificationServiceInterface {

    public void logError(String node_id, String msgType, String payload, Exception ex);
}
