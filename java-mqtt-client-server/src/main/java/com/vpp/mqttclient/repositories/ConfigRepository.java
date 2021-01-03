package com.vpp.mqttclient.repositories;

import com.vpp.mqttclient.domains.Config;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ConfigRepository extends MongoRepository<Config, String> {
    public List<Config> findByNodeIdAndTestCaseNameOrderByTimestamp(String nodeId, String testCaseName);
}
