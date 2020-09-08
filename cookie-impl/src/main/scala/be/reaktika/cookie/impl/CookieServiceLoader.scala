package be.reaktika.cookie.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import be.reaktika.cookie.api.CookieService
import be.reaktika.cookie.impl.readside.{CookieProcessor, CookieRepository}
import be.reaktika.cookie.impl.state.Cookie
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.api.{Descriptor, LagomConfigComponent, ServiceLocator}
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader, LagomServer, LagomServerComponents}
import play.api.db.HikariCPComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._

class CookieServiceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CookieServiceApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CookieServiceApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] =
    Some(readDescriptor[CookieService])
}

abstract class CookieServiceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with LagomServerComponents
    with LagomConfigComponent
    with SlickPersistenceComponents
    with HikariCPComponents
    with AhcWSComponents
    with LagomKafkaComponents {

  val serviceLocator: ServiceLocator

  lazy val userProfileRepository: CookieRepository =
    wire[CookieRepository]
  readSide.register(wire[CookieProcessor])

  lazy val cookieService: CookieService = wire[CookieServiceImpl]

  override lazy val lagomServer: LagomServer = serverFor[CookieService](cookieService)

  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = CookieServiceSerializerRegistry

  clusterSharding.init(Entity(Cookie.typeKey) { entityContext =>
    Cookie(entityContext)
  })
}
