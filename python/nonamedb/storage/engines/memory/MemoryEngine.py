from nonamedb.storage.Engine import Engine
from nonamedb.storage.engines.memory.EngineActor import EngineActor, Get, Put


class MemoryEngine(Engine):
    def __init__(self, actor_system, request_timeout):
        self.__system = actor_system
        self.__timeout = request_timeout
        self.__store = self.__system.createActor(EngineActor)

    def get(self, key):
        result = self.__system.ask(self.__store, Get(key), self.__timeout)
        return result

    def put(self, key, value):
        result = self.__system.ask(self.__store, Put(key, value), self.__timeout)
        return result
