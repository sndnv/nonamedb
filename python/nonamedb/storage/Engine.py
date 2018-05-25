from abc import ABC, abstractmethod


class Engine(ABC):
    @abstractmethod
    def get(self, key): pass

    @abstractmethod
    def put(self, key, value): pass
