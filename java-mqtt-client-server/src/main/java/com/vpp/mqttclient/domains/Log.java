package com.vpp.mqttclient.domains;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.Date;
@Data
public class Log {

    @Id
    public String id;
    private Date timestamp;	// Zulu time timestamp
    private String node_id;
    private String messageType;
    private String payload;
    private String errorMessage;

}
