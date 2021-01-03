package com.vpp.mqttclient.repositories;

import com.vpp.mqttclient.domains.Location;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LocationRepository extends MongoRepository<Location, String> {
}
