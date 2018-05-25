import logging
from thespian.actors import *


class Put:
    def __init__(self, key, value):
        self._key = key
        self._value = value

    @property
    def key(self): return self._key

    @property
    def value(self): return self._value


class Get:
    def __init__(self, key):
        self._key = key

    @property
    def key(self): return self._key


class EngineActor(Actor):
    def __init__(self):
        super().__init__()
        self.__store = dict()

    def receiveMessage(self, msg, sender):
        if isinstance(msg, Put):
            value = msg.value
            if isinstance(value, bytes):
                if value:
                    if msg.key in self.__store:
                        logging.debug("[PUT] Updating value with key [{}]".format(msg.key))
                    else:
                        logging.debug("[PUT] Adding value with key [{}]".format(msg.key))

                    self.__store[msg.key] = msg.value
                else:
                    logging.debug("[PUT] Removing value with key [{}]".format(msg.key))
                    if msg.key in self.__store:
                        del self.__store[msg.key]

                self.send(sender, True)
            else:
                logging.error("[PUT] Failed to put value with key [{}]; expected bytes value".format(msg.key))
                self.send(sender, False)

        elif isinstance(msg, Get):
            result = self.__store.get(msg.key)

            if result:
                logging.debug("[GET] Value with key [{}] found".format(msg.key))
            else:
                logging.debug("[GET] Value with key [{}] not found".format(msg.key))

            self.send(sender, result)

