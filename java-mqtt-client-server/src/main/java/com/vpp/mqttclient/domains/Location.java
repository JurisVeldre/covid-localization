package com.vpp.mqttclient.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class Location {
    @Id
    public String id;
    private String timestamp;	// Zulu time timestamp
    private String tag_id;		// tag ID of the sensor
    private String net_id;		// Network ID - optional
    private String zone_id;	// Zone ID within network
    private double x_coordinate;
    private double y_coordinate;
    private double z_coordinate;		// optional, default is 0
    private int quality;	// Quality of the measurement
    private String name;
    private Date created;
    private int counter;


}
