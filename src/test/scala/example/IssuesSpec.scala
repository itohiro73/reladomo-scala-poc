package example

import java.sql.{DriverManager, Timestamp}
import java.util.Calendar

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
    it("updates attributes with non-infinite businessTo attribute without touching primary key") {
      withTransaction { implicit tx =>
        val parentObject = NewParentObject(name = "name").insert()
        NewBitemporalChildObject(name = "name", state = 1, parentObject.id)
          .insertUntil(createTimestamp(9000, 1, 1)) //using long time away so that the test is practically deterministic
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

  private[this] def createTimestamp(year: Int,
                                    month: Int,
                                    dayOfMonth: Int): Timestamp = {
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month-1)
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    cal.set(Calendar.AM_PM, Calendar.AM)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    new Timestamp(cal.getTimeInMillis)
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
