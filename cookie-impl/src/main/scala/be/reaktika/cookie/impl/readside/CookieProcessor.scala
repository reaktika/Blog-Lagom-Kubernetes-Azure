package be.reaktika.cookie.impl.readside

import be.reaktika.cookie.impl.state.Cookie.{ CookieCreated, Event }
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEventTag, ReadSideProcessor }

class CookieProcessor(readSide: SlickReadSide, repository: CookieRepository) extends ReadSideProcessor[Event] {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[Event] =
    readSide
      .builder[Event]("cookie-processor")
      .setGlobalPrepare(repository.createTable())
      .setEventHandler[CookieCreated] { envelope =>
        val cookieId = envelope.entityId
        val cookieName = envelope.event.name
        val amount = 5
        repository.addCookie(cookieId, cookieName, amount)
      }
      .build()

  override def aggregateTags: Set[AggregateEventTag[Event]] = Event.Tag.allTags
}
