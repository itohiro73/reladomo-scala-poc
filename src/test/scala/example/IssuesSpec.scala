package example

import java.sql.DriverManager

import com.folio_sec.reladomo.scala_api.TransactionProvider.withTransaction
import com.folio_sec.reladomo.scala_api.configuration.DatabaseManager
import com.folio_sec.reladomo.scala_api.util.{LoanPattern, TimestampUtil}
import kata.domain.scala_api.{BitemporalChildObjectFinder, NewBitemporalChildObject, NewParentObject, ParentObjectFinder}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scala.io.Source

class IssuesSpec extends FunSpec with Matchers with BeforeAndAfter {

  before {
    initializeDatabase()
  }

  describe("Related object with non existent condition") {
    it("should return None") {
      withTransaction{ implicit tx =>
        NewParentObject(name = "name").insert()
      }

      val maybeParent = ParentObjectFinder.findOneWith(_.name.eq("name"))

      maybeParent match {
        case Some(parent) =>
          parent.relatedObject(TimestampUtil.now()) should equal(None)
      }
    }
  }

  describe("Bi-temporal object") {
    it("updates attributes without touching primary key") {
      withTransaction { implicit tx =>
        val parentObject = NewParentObject(name = "name").insert()
        NewBitemporalChildObject(name = "name", state = 1, parentObject.id).insert()
      }


      val maybeParent = ParentObjectFinder.findOneWith(_.name.eq("name"))
      maybeParent match {
        case Some(parent) =>

          val child = parent.relatedObject(TimestampUtil.now())

          withTransaction{ implicit tx =>
            val updatedChild = child.copy(name = "updated name")
            updatedChild.update()
          }
      }

      val maybeUpdatedChild = BitemporalChildObjectFinder.findOneWith(_.businessDate.eq(TimestampUtil.now()))

      maybeUpdatedChild match {
        case Some(child) =>
          child.name should equal("updated name")
      }
    }

  }

  Class.forName("org.h2.Driver")
  val jdbcUrl = "jdbc:h2:mem:sample;MODE=MySQL;TRACE_LEVEL_FILE=2;TRACE_LEVEL_SYSTEM_OUT=2"
  val jdbcUser = "user"
  val jdbcPass = "pass"

  def initializeDatabase(): Unit = {
    val initialDdls = Source.fromResource("initial.sql").mkString
    LoanPattern.using(DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPass)) { conn =>
      conn.prepareStatement(initialDdls).execute()
      DatabaseManager.loadRuntimeConfig("ReladomoRuntimeConfig.xml")
    }
  }

}
