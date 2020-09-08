package be.reaktika.cookieanalytics.impl

import be.reaktika.cookie.api.CookieService
import be.reaktika.cookieanalytics.api.CookieAnalyticsService
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.api.Descriptor
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer}
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire.wire

class CookieAnalyticsServiceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CookieAnalyticsServiceApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CookieAnalyticsServiceApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] =
    Some(readDescriptor[CookieAnalyticsService])

}

abstract class CookieAnalyticsServiceApplication(context: LagomApplicationContext)
    extends LagomApplication(context)
    with AhcWSComponents
    with LagomKafkaClientComponents {

  lazy val cookieService: CookieService = serviceClient.implement[CookieService]

  override lazy val lagomServer: LagomServer =
    serverFor[CookieAnalyticsService](wire[CookieAnalyticsServiceImpl])
}
