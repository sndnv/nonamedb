package nonamedb.test.specs.unit

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

trait UnitSpec extends AsyncFlatSpec with Matchers with AsyncIOSpec
