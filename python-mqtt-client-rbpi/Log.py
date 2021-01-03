from bson.objectid import ObjectId
import datetime

class Log(object):


    def __init__(self,
                 id=None,
                 node_id=None,
                 message_type=None,
                 payload=None,
                 error_message=None):
        if id is None:
            self._id = ObjectId()
        else:
            self._id = id

        self.node_id = node_id
        self.message_type = message_type
        self.payload = payload
        self.error_message = error_message
        self.time = datetime.datetime.now()

    def get_as_json(self):
        return self.__dict__