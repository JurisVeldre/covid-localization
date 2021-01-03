package com.vpp.mqttclient.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class Distance {

    @Id
    public String id;
    private String timestamp;	// Zulu time timestamp
    private String tag1_id;	// tag ID of the first sensor
    private String tag2_id;   // tag ID of the second sensor
    private String zone_id;	// Zone ID within network
    private double distance;	// observed distance
    private int quality;	// Quality of the measurement
    private String name;
    private int counter;
    private Date created;

}
