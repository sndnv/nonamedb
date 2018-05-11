package nonamedb.storage

import akka.Done

import scala.concurrent.Future

trait Engine {
  def get(key: Key): Future[Option[Value]]
  def put(key: Key, value: Value): Future[Done]
}
