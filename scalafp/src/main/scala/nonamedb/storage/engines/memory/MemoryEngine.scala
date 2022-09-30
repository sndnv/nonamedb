package nonamedb.storage.engines.memory

import cats.effect._
import cats.implicits._
import nonamedb.storage._

class MemoryEngine[F[_]: Async](store: Ref[F, Map[Key, Value]]) extends Engine[F] {
  override def get(key: Key): F[Option[Value]] = store.get.map(_.get(key))
  override def put(key: Key, value: Value): F[Unit] = store.update {
    case map if value.isEmpty => map - key
    case map                  => map + (key -> value)
  }
}

object MemoryEngine {
  def apply[F[_]: Async](): F[MemoryEngine[F]] =
    Ref[F].of(Map.empty[Key, Value]).map(new MemoryEngine(_))
}
