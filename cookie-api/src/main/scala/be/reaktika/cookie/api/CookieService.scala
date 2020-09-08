package be.reaktika.cookie.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import com.lightbend.lagom.scaladsl.api.transport.Method

trait CookieService extends Service {

  def getCookieNames: ServiceCall[NotUsed, List[String]]

  def createCookie(cookieId: String, cookieName: String): ServiceCall[NotUsed, String]

  def cookieEventsOutTopic: Topic[CookieEvents.CookieEvent]

  override final def descriptor: Descriptor = {
    import Service._
    import CookieEvents.EventFormat._

    named("cookie-service")
      .withCalls(
        restCall(Method.GET, s"/api/cookie-service/cookies", getCookieNames _),
        restCall(Method.POST, s"/api/cookie-service/cookie/:cookieId/:cookieName", createCookie _)
      )
      .withTopics(
        topic(CookieEvents.TOPIC, cookieEventsOutTopic)
      )
      .withAutoAcl(true)
  }

}
