package be.reaktika.monster.impl

import be.reaktika.cookie.api.CookieService
import be.reaktika.monster.api.MonsterService
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.api.{Descriptor, LagomConfigComponent, ServiceLocator}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

class MonsterServiceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new MonsterServiceApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new MonsterServiceApplication(context) with LagomDevModeComponents

  override def describeService: Option[Descriptor] =
    Some(readDescriptor[CookieService])
}

abstract class MonsterServiceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with LagomServerComponents
    with LagomConfigComponent
    with AhcWSComponents {

  val serviceLocator: ServiceLocator

  lazy val cookieService: CookieService = serviceClient.implement[CookieService]

  lazy val monsterService: MonsterService = wire[MonsterServiceImpl]

  override lazy val lagomServer: LagomServer = serverFor[MonsterService](monsterService)
}
