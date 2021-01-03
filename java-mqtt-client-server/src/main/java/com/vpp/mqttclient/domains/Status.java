package com.vpp.mqttclient.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
@Data
public class Status {

    @Id
    public String id;
    private Date timestamp;	// Zulu time timestamp
    private String nodeId;
    private boolean present; // 1 - true; 0 - false
    private Date created;
}
