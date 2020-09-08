package be.reaktika.monster.api

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

trait MonsterService extends Service {

  def getCookieNames: ServiceCall[NotUsed, List[String]]

  override final def descriptor: Descriptor = {
    import Service._

    named("monster-service")
      .withCalls(
        restCall(Method.GET, s"/api/monster-service/cookies", getCookieNames _)
      )
      .withAutoAcl(true)
  }

}
