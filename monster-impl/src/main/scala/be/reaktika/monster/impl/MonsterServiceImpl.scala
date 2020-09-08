package be.reaktika.monster.impl

import akka.NotUsed
import akka.util.Timeout
import be.reaktika.cookie.api.CookieService
import be.reaktika.monster.api.MonsterService
import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

class MonsterServiceImpl(cookieService: CookieService)(implicit ec: ExecutionContext)
    extends MonsterService {

  implicit val timeout: Timeout = Timeout(30.seconds)

  override def getCookieNames: ServiceCall[NotUsed, List[String]] = ServiceCall { _ =>
    cookieService.getCookieNames.invoke().map {
      case Nil => Nil
      case cookies => cookies.take(cookies.size / 2)
    }
  }

}
