package be.reaktika.cookie.impl

import akka.NotUsed
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.util.Timeout
import be.reaktika.cookie.api.{ CookieEvents, CookieService }
import be.reaktika.cookie.impl.readside.CookieRepository
import be.reaktika.cookie.impl.state.Cookie
import be.reaktika.cookie.impl.state.Cookie.{ Command, Confirmation, CookieCreated, CreateCookie, Event }
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{ EventStreamElement, PersistentEntityRegistry }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

class CookieServiceImpl(
    clusterSharding: ClusterSharding,
    persistentEntityRegistry: PersistentEntityRegistry,
    cookieRepository: CookieRepository)(implicit ec: ExecutionContext)
    extends CookieService {

  implicit val timeout: Timeout = Timeout(30.seconds)

  private def entityRef(cookieId: String): EntityRef[Command] =
    clusterSharding.entityRefFor(Cookie.typeKey, cookieId)

  override def getCookieNames: ServiceCall[NotUsed, List[String]] = ServiceCall { _ =>
    cookieRepository.listCookies().map { cookies =>
      cookies.map(_.name).toList
    }
  }

  override def createCookie(cookieId: String, cookieName: String): ServiceCall[NotUsed, String] = ServiceCall { _ =>
    entityRef(cookieId).ask[Confirmation](reply => CreateCookie(cookieName, reply)).map {
      case Cookie.Accepted(message)  => message
      case Cookie.BadRequest(reason) => reason
    }
  }

  override def cookieEventsOutTopic: Topic[CookieEvents.CookieEvent] = {
    TopicProducer.taggedStreamWithOffset(Event.Tag) { (tag, fromOffset) =>
      persistentEntityRegistry.eventStream(tag, fromOffset).mapAsync(4) {
        case EventStreamElement(_, event, offset) =>
          val kafkaEvent = event match {
            case CookieCreated(cookieName) =>
              CookieEvents.CookieAdded(cookieName)
          }
          Future.successful(kafkaEvent -> offset)
      }
    }
  }
}
