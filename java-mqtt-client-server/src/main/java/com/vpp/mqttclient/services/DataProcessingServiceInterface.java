package com.vpp.mqttclient.services;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
public interface DataProcessingServiceInterface {
    public void onMessage(String topic, MqttMessage message);
}
