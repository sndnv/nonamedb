package nonamedb.storage.engines.memory

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import nonamedb.storage.{Key, Value}

class EngineActor extends Actor with ActorLogging {
  import EngineActor._

  private def process(store: Map[Key, Value]): Receive = {
    case Put(key, value) =>
      val updatedStore: Map[Key, Value] =
        if (value.isEmpty) {
          log.debug(
            "[PUT] Removing value with key [{}]",
            key
          )

          store - key
        } else {
          log.debug(
            "[PUT] {} value with key [{}]",
            if (store.get(key).isDefined) "Updating" else "Adding",
            key
          )

          store + (key -> value)
        }

      sender ! Done
      context.become(process(updatedStore))

    case Get(key) =>
      val result: Option[Value] = store.get(key)

      log.debug(
        "[GET] Value with key [{}] {}",
        key,
        if (result.isDefined) "found" else "not found"
      )

      sender ! result
  }

  override def receive: Receive = process(Map.empty)
}

object EngineActor {
  final case class Put(key: Key, value: Value)
  final case class Get(key: Key)

  def props(): Props = Props(
    classOf[EngineActor]
  )
}
