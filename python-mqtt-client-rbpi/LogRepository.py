from pymongo import MongoClient
from urllib.parse import quote


class LogRepository(object):

    def __init__(self):
        # initializing the MongoClient
        self.client = MongoClient(
            "mongodb://VKhlous:" + quote(
                "Lg@YUMlowp") + "@13.48.133.235:27017/?authSource=conTraDB&readPreference=primary&appname=MongoDB%20Compass&ssl=false")
        self.database = self.client['conTraDB']

    def create(self, log):
        if log is not None:
            self.database.Log.insert(log.get_as_json())
        else:
            raise Exception("Nothing to save, because project parameter is None")

    def read(self, id=None):
        if id is None:
            return self.database.TagLocations.find({})
        else:
            return self.database.TagLocations.find({"_id":id})

    def update(self, log):
        if log is not None:
            # the save() method updates the document if this has an _id property
            # which appears in the collection, otherwise it saves the data
            # as a new document in the collection
            self.database.Log.save(log.get_as_json())
        else:
            raise Exception("Nothing to update, because project parameter is None")

    def delete(self, log):
        if log is not None:
            self.database.TagLocations.remove(log.get_as_json())
        else:
            raise Exception("Nothing to delete, because project parameter is None")