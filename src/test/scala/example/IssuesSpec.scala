package example

import java.sql.DriverManager

import com.folio_sec.reladomo.scala_api.TransactionProvider.withTransaction
import com.folio_sec.reladomo.scala_api.configuration.DatabaseManager
import com.folio_sec.reladomo.scala_api.util.TimestampUtil.now
import com.folio_sec.reladomo.scala_api.util.{LoanPattern, TimestampUtil}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

import scala.io.Source

class IssuesSpec extends FunSpec with Matchers with BeforeAndAfter {

  before {
    initializeDatabase()
  }

  describe("Related object with non existent condition") {
    it("should return without null pointer exception") {

    }
  }

  describe("Bi-temporal object") {
    it("updates attributes without touching primary key") {

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
