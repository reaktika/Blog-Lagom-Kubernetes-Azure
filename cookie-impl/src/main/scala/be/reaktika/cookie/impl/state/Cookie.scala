package be.reaktika.cookie.impl.state

import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.sharding.typed.scaladsl.{EntityContext, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect, RetentionCriteria}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger, AkkaTaggerAdapter}
import play.api.libs.json.{Format, Json}

object Cookie {

  // Commands
  trait CommandSerializable

  sealed trait Command extends CommandSerializable

  final case class CreateCookie(cookieName: String, replyTo: ActorRef[Confirmation]) extends Command

  // Confirmation
  sealed trait Confirmation

  final case class Accepted(message: String) extends Confirmation

  final case class BadRequest(reason: String) extends Confirmation

  implicit val confirmationAcceptedFormat: Format[Accepted] = Json.format
  implicit val confirmationBadRequestFormat: Format[BadRequest] = Json.format

  // Events
  sealed trait Event extends AggregateEvent[Event] {
    override def aggregateTag: AggregateEventTagger[Event] = Event.Tag
  }
  object Event {
    val Tag: AggregateEventShards[Event] = AggregateEventTag.sharded[Event](numShards = 10)
  }

  final case class CookieCreated(name: String) extends Event

  implicit val cookieCreatedFormat: Format[CookieCreated] = Json.format

  implicit val cookieFormat: Format[Cookie] = Json.format

  def apply(entityContext: EntityContext[Command]): Behavior[Command] =
    apply(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
      .withTagger(AkkaTaggerAdapter.fromLagom(entityContext, Event.Tag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 100, keepNSnapshots = 2))

  def apply(persistenceId: PersistenceId): EventSourcedBehavior[Command, Event, Cookie] = {
    EventSourcedBehavior.withEnforcedReplies[Command, Event, Cookie](
      persistenceId = persistenceId,
      emptyState = Cookie.empty,
      commandHandler = (cookie, cmd) => cookie.applyCommand(cmd),
      eventHandler = (cookie, evt) => cookie.applyEvent(evt))
  }

  val empty: Cookie = Cookie(name = None)

  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Cookie")

}

case class Cookie(name: Option[String]) {
  import Cookie._

  def applyCommand(cmd: Command): ReplyEffect[Event, Cookie] = {
    cmd match {
      case CreateCookie(cookieName, replyTo) =>
        Effect
          .persist(CookieCreated(cookieName))
          .thenReply(replyTo)(_ => Accepted(s"Cookie with name $cookieName created."))
    }
  }

  def applyEvent(evt: Event): Cookie = {
    evt match {
      case CookieCreated(name) =>
        copy(name = Some(name))
    }
  }
}
