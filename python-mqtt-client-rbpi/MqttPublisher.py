import paho.mqtt.client as mqtt


def on_subscribe():
    print("Subscribed to queue")


def on_connect(port):
    print("Connected to MQTT broker on port %s", port)


class MqttPublisher(object):

    def __init__(self, name):
        # initializing the MongoClient
        self.broker_address = "localhost"
        self.mqtt_client = mqtt.Client(name)

    def connect(self, port):
        self.mqtt_client.on_connect = on_connect(port)
        self.mqtt_client.connect(host=self.broker_address, port=port, keepalive=10)

    def publish(self, tag_id, topic, json):
        self.mqtt_client.publish(topic="dwm/node/" + str(tag_id) + "/uplink/" + str(topic), payload=json, qos=0)
