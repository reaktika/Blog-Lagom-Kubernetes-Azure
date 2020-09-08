package be.reaktika.cookie.impl.readside

import akka.Done
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import slick.dbio.Effect
import slick.sql.FixedSqlAction
import slick.jdbc.PostgresProfile.api._

class CookieRepository(database: Database) {
  class CookieTable(tag: Tag) extends Table[CookieReport](tag, "cookies") {

    def id = column[String]("id", O.PrimaryKey)
    def name = column[String]("name")
    def amount = column[Int]("amount")

    def * =
      (id, name, amount) <> ((CookieReport.apply _).tupled, CookieReport.unapply)
  }

  val cookieTable = TableQuery[CookieTable]

  def createTable(): FixedSqlAction[Unit, NoStream, Effect.Schema] = cookieTable.schema.createIfNotExists

  def addCookie(id: String, name: String, amount: Int): DBIO[Done] = {
    val cookieReport = CookieReport(id, name, amount)
    (cookieTable += cookieReport).map(_ => Done).transactionally
  }

  def listCookies(): Future[Seq[CookieReport]] = {
    database.run(cookieTable.result)
  }
}
