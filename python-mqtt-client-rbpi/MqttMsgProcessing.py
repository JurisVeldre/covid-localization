import json
import datetime

from MqttPublisher import MqttPublisher
from Log import Log
from LogRepository import LogRepository

mqtt_publisher = MqttPublisher("publisher")
mqtt_publisher.connect(1888)


class MqttMsgProcessing(object):

    @staticmethod
    def process_message(message, counter):
        tag_id = None
        msg_type = None
        msg_text = None
        try:
            print("Processing message...")
            msg_text = str(message.payload.decode("utf-8"))
            topic = message.topic.split('/')
            tag_id = topic[2]
            msg_type = topic[4]

            json_msg = json.loads(msg_text)
            json_msg['timestamp'] = datetime.datetime.now()
            json_msg['counter'] = counter

            if msg_type == "config":
                json_msg["zoneId"] = "zoneId"
                json_msg["panId"] = "panId"
                json_msg["active"] = True

            mqtt_publisher.publish(tag_id, topic[4], json.dumps(json_msg, default=str))
            f = open("backlog.txt", "a")
            f.write(tag_id + "," + topic[4] + "," +json.dumps(json_msg, default=str))
            f.write('\n')
            f.close()
            
            print("Processed.")
        except Exception as e:
            print("<p>Error: %s</p>" % str(e))
            try:
                log = Log(None, tag_id, msg_type, msg_text, e)
                repository = LogRepository()
                repository.create(log)
            except Exception as ex:
                print("<p>Error: %s</p>" % str(ex))

