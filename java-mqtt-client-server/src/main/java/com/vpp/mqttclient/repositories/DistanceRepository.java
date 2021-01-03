package com.vpp.mqttclient.repositories;

import com.vpp.mqttclient.domains.Distance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DistanceRepository extends MongoRepository<Distance, String> {
}
