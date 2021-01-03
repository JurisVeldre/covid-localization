package com.vpp.mqttclient;
import com.vpp.mqttclient.services.MQTTListenerInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class MqttClientApplication  implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(MqttClientApplication.class, args);
	}

	@Autowired
	private
	MQTTListenerInterface MQTTListener;


	@Override
	public void run(ApplicationArguments args) throws Exception {

		MQTTListener.connectToMQTT();
		//test();
	}

}
