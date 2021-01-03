package com.vpp.mqttclient.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class Config {
    @Id
    public String id;
    private String zoneId;
    private int panId;
    private String nodeId;
    private String testCaseName;
    private String nodeType;
    private String timestamp;

    // Anchor information
    private boolean initiator;
    private Double xCoordinate;
    private Double yCoordinate;
    private Double zCoordinate;
    private int quality;

    // Tag information
    private boolean stationaryDetection;
    private boolean responsive;
    private boolean locationEngine;
    private int nomUpdateRate;
    private int statUpdateRate;

    private boolean active;
    private Date created;
}
