package com.vpp.mqttclient.services;

import org.springframework.stereotype.Component;

@Component
public interface MQTTListenerInterface {
    void connectToMQTT() throws Exception;
}
