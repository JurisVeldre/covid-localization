import paho.mqtt.client as mqtt
from MqttMsgProcessing import MqttMsgProcessing

counter = 0
service = MqttMsgProcessing


def on_message(client, userdata, message):
    print("message received ", str(message.payload.decode("utf-8")))
    print("message topic=", message.topic)
    print("message qos=", message.qos)
    print("message retain flag=", message.retain)
    global counter
    counter = counter + 1
    service.process_message(message, counter)


def on_subscribe():
    print("Subscribed to queue")


# def on_connect(port, mqtt_client):
#     mqtt_client.subscribe("dwm/node/+/uplink/+")
#     print("Connected to MQTT broker on port %s", port)

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("connected OK")
        client.subscribe("dwm/node/+/uplink/+")
    else:
        print("Bad connection Returned code=",rc)


def on_disconnect():
    print("DISCONNECTED!")


class MqttListener(object):

    def __init__(self, name):
        # initializing the MongoClient
        self.broker_address = "localhost"  # Localhost
        self.mqtt_client = mqtt.Client(name, clean_session=False)

    def connect(self, port):
        self.mqtt_client.on_message = on_message
        self.mqtt_client.on_connect = on_connect
        self.mqtt_client.on_subscribe = on_subscribe()
        self.mqtt_client.on_disconnect = on_disconnect()
        self.mqtt_client.connect(host=self.broker_address, port=port, keepalive=10)
        self.mqtt_client.loop_start()

    def start_listening(self):
        self.mqtt_client.subscribe("dwm/node/+/uplink/+")
        self.mqtt_client.loop_start()  # start the loop

    def publish(self, tag_id, topic, json):
        self.mqtt_client.publish(topic="dwm/node/" + str(tag_id) + "/uplink/" + str(topic), payload=json, qos=0)
