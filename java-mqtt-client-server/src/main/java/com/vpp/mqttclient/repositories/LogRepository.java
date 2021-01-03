package com.vpp.mqttclient.repositories;

import com.vpp.mqttclient.domains.Log;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<Log, String> {
}
