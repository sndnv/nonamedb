package nonamedb.storage

import cats.effect.Async

abstract class Engine[F[_]: Async] {
  def get(key: Key): F[Option[Value]]
  def put(key: Key, value: Value): F[Unit]
}
