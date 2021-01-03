package com.vpp.mqttclient.repositories;

import com.vpp.mqttclient.domains.Status;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StatusRepository extends MongoRepository<Status, String> {

}
