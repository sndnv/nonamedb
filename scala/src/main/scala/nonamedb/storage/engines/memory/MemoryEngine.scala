package nonamedb.storage.engines.memory

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import nonamedb.storage.engines.memory.EngineActor.{Get, Put}
import nonamedb.storage.{Engine, Key, Value}

import scala.concurrent.Future

class MemoryEngine()(implicit system: ActorSystem, timeout: Timeout) extends Engine {
  private val store: ActorRef = system.actorOf(EngineActor.props())
  override def get(key: Key): Future[Option[Value]] = (store ? Get(key)).mapTo[Option[Value]]
  override def put(key: Key, value: Value): Future[Done] = (store ? Put(key, value)).mapTo[Done]
}
