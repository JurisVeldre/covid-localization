package com.vpp.mqttclient.services;

import com.vpp.mqttclient.domains.Config;
import com.vpp.mqttclient.domains.Distance;
import com.vpp.mqttclient.domains.Location;
import com.vpp.mqttclient.domains.Status;
import com.vpp.mqttclient.repositories.ConfigRepository;
import com.vpp.mqttclient.repositories.DistanceRepository;
import com.vpp.mqttclient.repositories.LocationRepository;
import com.vpp.mqttclient.repositories.StatusRepository;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class DataProcessingService implements DataProcessingServiceInterface {

    private DistanceRepository distanceRepository;
    private LocationRepository locationRepository;

    private StatusRepository statusRepository;
    private NotificationService notificationService;
    private LoggingService loggingService;
    private ConfigRepository configRepository;

    private static final String C_MSG_TYPE_LOCATION = "location";
    private static final String C_MSG_TYPE_DATA = "data";
    private static final String C_MSG_TYPE_CONFIG = "config";
    private static final String C_MSG_TYPE_STATUS = "status";

    private TreeMap map;


    public DataProcessingService(DistanceRepository distanceRepository,
                                 LocationRepository locationRepository,
                                 StatusRepository statusRepository,
                                 NotificationService notificationService,
                                 LoggingService loggingService,
                                 ConfigRepository configRepository) throws IOException {
        this.distanceRepository = distanceRepository;
        this.locationRepository = locationRepository;
        this.statusRepository = statusRepository;
        this.notificationService = notificationService;
        this.loggingService = loggingService;
        this.configRepository = configRepository;

        //@Value("${dwm.properties.file.location}")
      //  String propertiesFileLocation = "./properties.txt";
        String propertiesFileLocation = "C:\\Users\\Valeriya Khlus\\IdeaProjects\\mqtt-client\\src\\main\\resources\\properties.txt";
        map = PropertiesFromFile.loadPropertiesFromFile(propertiesFileLocation);

    }

    public class Topic
    {
        String nodeId;
        String msgType;
        String originalTopic;
    }


    @Override
    public void onMessage(String topicStr, MqttMessage message) {
        Topic topic = null;
        String payload = null;
        try {
            log.info("Processing message..");
            topic = createTopicData(topicStr);
            log.info("Node id: " + topic.nodeId);
            log.info("Msq type: " + topic.msgType);

            if (message == null || message.getPayload() == null || message.getPayload().length == 0)
            {
                throw new Exception("Payload is empty.");
            }

            payload = new String(message.getPayload());

            processMessage(topic, payload);

            log.info("Message processed.");
        } catch (Exception ex){
            if (topic != null) {
                log.error("Error processing the message.");
                loggingService.logError(topic.nodeId , topic.msgType, payload, ex);
                notificationService.logError(topic.nodeId, topic.msgType, payload, ex);
            } else {

                log.error("Error processing the message.");
                loggingService.logError(null, null, payload, ex);
                notificationService.logError(null, null, payload, ex);
            }

            log.error("Error description: ", ex);

        }
    }

    private void processMessage(Topic topic, String message) throws Exception {
        if (topic.msgType.equals(C_MSG_TYPE_LOCATION)){
            processLocationMessage(topic, message);
        } else if (topic.msgType.equals(C_MSG_TYPE_DATA)){
            processDataMessage(topic, message);
        } else if (topic.msgType.equals(C_MSG_TYPE_CONFIG)) {
            processConfigMessage(topic, message);
        } else if (topic.msgType.equals(C_MSG_TYPE_STATUS)) {
            processStatusMessage(topic, message);
        } else {
            throw new Exception("Unknown message topic. Topic: " + topic.msgType);
        }

    }

    private void processStatusMessage(Topic topic, String message) {
        JSONObject obj = new JSONObject(message);
        boolean present = obj.getBoolean("present");

        // Jāizmanto tikai, kad ir false, jo, kad ir true, tad tiek ielikts config ieraksts
        if (!present) {
            // Atjaunot ierakstu, kas atbilst šī mezgla konfigam
            //  Lai būtu trasējamība, tikai atjaunojam "active" lauku uz false
            List<Config> records= configRepository.findByNodeIdAndTestCaseNameOrderByTimestamp(topic.nodeId, map.get("dwm.test-name").toString());

            if (records != null && records.size() > 0 ) {
                int size = records.size();
                Config recordToUpdate = records.get(size - 1);
                recordToUpdate.setActive(false);
                configRepository.save(recordToUpdate);
            }
        }

        Status status = new Status();

        status.setNodeId(topic.nodeId);
        status.setPresent(present);
        status.setCreated(new Date(System.currentTimeMillis()));

        statusRepository.save(status);
    }

    private void processConfigMessage(Topic topic, String message) {
        // Te nevajadzētu neko mainīt, bet tikai nosūtīt uz attiecīgo kolekciju,
        //  jo dati jau tiek papildināti uz rbpi
        JSONObject obj = new JSONObject(message);

        String zoneId = obj.getString("zoneId");
        int panId = obj.getInt("panId");
        String timestamp = obj.getString("timestamp");
        String nodeType = obj.getJSONObject("configuration").getString("nodeType");

        if (nodeType.equals("ANCHOR")) {
            Config configEntity = new Config();
            JSONObject anchor = obj.getJSONObject("configuration").getJSONObject("anchor");

            boolean initiator = anchor.getBoolean("initiator");
            Double x = anchor.getJSONObject("position").getDouble("x");
            Double y = anchor.getJSONObject("position").getDouble("y");
            Double z = anchor.getJSONObject("position").getDouble("z");
            int quality = anchor.getJSONObject("position").getInt("quality");

            configEntity.setZoneId(zoneId);
            configEntity.setPanId(panId);
            configEntity.setNodeId(topic.nodeId);
            configEntity.setTestCaseName(map.get("dwm.test-name").toString());
            configEntity.setNodeType(nodeType);

            configEntity.setInitiator(initiator);
            configEntity.setXCoordinate(x);
            configEntity.setYCoordinate(y);
            configEntity.setZCoordinate(z);
            configEntity.setQuality(quality);
            configEntity.setTimestamp(timestamp);

            configEntity.setActive(true);


            configEntity.setCreated(new Date(System.currentTimeMillis()));
            configRepository.save(configEntity);
        } else if (nodeType.equals("TAG")) {
            Config configEntity = new Config();
            JSONObject tag = obj.getJSONObject("configuration").getJSONObject("tag");

            boolean stationaryDetection = tag.getBoolean("stationaryDetection");
            boolean responsive = tag.getBoolean("responsive");
            boolean locationEngine = tag.getBoolean("locationEngine");
            int nomUpdateRate = tag.getInt("nomUpdateRate");
            int statUpdateRate = tag.getInt("statUpdateRate");

            configEntity.setZoneId(zoneId);
            configEntity.setPanId(panId);
            configEntity.setNodeId(topic.nodeId);
            configEntity.setTestCaseName(map.get("dwm.test-name").toString());
            configEntity.setNodeType(nodeType);

            configEntity.setStationaryDetection(stationaryDetection);
            configEntity.setResponsive(responsive);
            configEntity.setLocationEngine(locationEngine);
            configEntity.setNomUpdateRate(nomUpdateRate);
            configEntity.setStatUpdateRate(statUpdateRate);
            configEntity.setTimestamp(timestamp);

            configEntity.setActive(true);

            configEntity.setCreated(new Date(System.currentTimeMillis()));
            configRepository.save(configEntity);
        }
    }

    private void processDataMessage(Topic topic, String message) {
        JSONObject obj = new JSONObject(message);
        String dataString = obj.getString("data");
        String timestamp = obj.getString("timestamp");
        byte[] decodedString = Base64.getDecoder().decode(dataString);

        if (decodedString.length >= 34) {
            int anchorsCount = decodedString[0];
            log.info("Count: " + anchorsCount);
            int counter = decodedString[anchorsCount * 4 + 1];
            log.info("Counter: " + counter);

            int bytesCounter = 1;
            List<Distance> distances = new LinkedList<Distance>();

            for (int i = 0; i < anchorsCount; i++) {
                int address = (decodedString[bytesCounter + 0] & 0xff) | ((decodedString[bytesCounter + 1]& 0xff) & 0x000000FF) << 8;
                String hexAddress = Integer.toHexString(address);
                log.info("Address: " + hexAddress);

                double distance = (decodedString[bytesCounter + 2]& 0xff) | ((decodedString[bytesCounter + 3]& 0xff) & 0x000000FF) << 8 | ((decodedString[bytesCounter + 4]& 0xff) & 0x000000FF) << 16 | ((decodedString[bytesCounter + 5]& 0xff) & 0x000000FF) << 24;
                log.info("Distance: " + distance);
                bytesCounter = bytesCounter + 4;

                Distance distanceEntity = new Distance();
                distanceEntity.setName(map.get("dwm.test-name").toString());
                distanceEntity.setTag1_id(topic.nodeId);
                distanceEntity.setTag2_id(hexAddress);
                distanceEntity.setDistance(distance);
                distanceEntity.setTimestamp(timestamp);
                distanceEntity.setCounter(counter);

                distanceEntity.setCreated(new Date(System.currentTimeMillis()));
                distances.add(distanceEntity);
            }


            distanceRepository.saveAll(distances);
        }
        else
        {
            log.info("GOT THE MESSAGE");
        }
    }

    private void processLocationMessage(Topic topic, String message) {
        JSONObject obj = new JSONObject(message);
        double x_coordinate = obj.getJSONObject("position").getDouble("x");
        double y_coordinate = obj.getJSONObject("position").getDouble("y");
        double z_coordinate = obj.getJSONObject("position").getDouble("z");
        String zone_id = obj.getString("zone_id");
        int counter = obj.getInt("counter");
        int quality = obj.getJSONObject("position").getInt("quality");
        String timestamp = obj.getString("timestamp");

        Location location = new Location();
        location.setX_coordinate(x_coordinate);
        location.setY_coordinate(y_coordinate);
        location.setZ_coordinate(z_coordinate);
        location.setZone_id(zone_id);
        location.setQuality(quality);
        location.setTimestamp(timestamp);
        location.setTag_id(topic.nodeId);
        location.setCounter(counter);
        location.setName(map.get("dwm.test-name").toString());

        location.setCreated(new Date(System.currentTimeMillis()));
        locationRepository.save(location);
    }

    private Topic createTopicData(String topicStr) throws Exception {
        if (topicStr == null)
        {
            throw new Exception("Topic is null!");
        }

        String[] topicSplit = topicStr.split("/");
        if (topicSplit.length < 5)
        {
            throw new Exception("Incorrect topic string. Topic: " + topicStr);
        }

        Topic topic = new Topic();
        topic.nodeId = topicSplit[2];
        topic.msgType = topicSplit[4];

        return topic;
    }
}
