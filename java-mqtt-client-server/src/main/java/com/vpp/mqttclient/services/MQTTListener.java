package com.vpp.mqttclient.services;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.UUID;

@Component
@Slf4j
public class MQTTListener implements MQTTListenerInterface {
    @Value("${mqtt.host}")
    private String HOST;
    @Value("${mqtt.automatic_reconnect}")
    private boolean automaticReconnect;
    @Value("${mqtt.clean_session}")
    private boolean cleanSession;
    @Value("${mqtt.connection_timeout}")
    private int connectionTimeout;
    @Value("${mqtt.queues}")
    private String queues;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.username}")
    private String userName;
    @Value("${mqtt.certificate.passphrase}")
    private String certificatePassphrase;


    private String listenerId =  "IOT_LISTENER_" + UUID.randomUUID().toString();
    private IMqttClient listener;
    private DataProcessingServiceInterface dataProcessingService;
    private NotificationServiceInterface notificationService;

    public MQTTListener(DataProcessingServiceInterface dataProcessingService,
                        NotificationServiceInterface notificationService)
    {
        this.dataProcessingService = dataProcessingService;
        this.notificationService = notificationService;
    }

    public void connectToMQTT() throws Exception {
        listener = new MqttClient(HOST, listenerId);
        java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        final char[] passphrase = certificatePassphrase.toCharArray();

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setConnectionTimeout(connectionTimeout);
        // MQTT broker password and user name
        options.setPassword(password.toCharArray());
        options.setUserName(userName);

        // server certificate. Certificate created from PEM certificate file to JKS keytool -importcert -alias rmq -file ./server_certificate.pem -keystore ./jvm_keystore
        KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(new FileInputStream("./jvm_keystore"), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(tks);
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(null, tmf.getTrustManagers(), null);
        options.setSocketFactory(ctx.getSocketFactory());

        listener.setCallback(new OnMessageCallback((DataProcessingService) dataProcessingService));
        listener.connect(options);
        listener.subscribe(queues, 1);
    }
}
