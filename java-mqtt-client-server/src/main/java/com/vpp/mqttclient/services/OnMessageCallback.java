package com.vpp.mqttclient.services;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

@Slf4j
public class OnMessageCallback  implements MqttCallback {

    private DataProcessingServiceInterface dataProcessingService;
    public OnMessageCallback(DataProcessingService service)
    {
        dataProcessingService = service;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("Disconnecting.. Reconnected here.");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
       log.info("Received message topic:" + topic);
       log.info("Received message Qos:" + mqttMessage.getQos());
       log.info("Received message content:" + new String(mqttMessage.getPayload()));

       dataProcessingService.onMessage(topic, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
