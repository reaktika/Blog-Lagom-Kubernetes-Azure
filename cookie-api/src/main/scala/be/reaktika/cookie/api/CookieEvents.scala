package be.reaktika.cookie.api

import julienrf.json.derived
import play.api.libs.json.OFormat
import play.api.libs.json._

object CookieEvents {
  val TOPIC = "events.cookie.out"

  sealed trait CookieEvent

  case class CookieAdded(cookieName: String) extends CookieEvent

  object EventFormat {
    val defaultTypeFormat: OFormat[String] = (__ \ "type").format[String]
    implicit val storyEventFormat: OFormat[CookieEvent] = derived.flat.oformat[CookieEvent](typeName = defaultTypeFormat)
  }
}
