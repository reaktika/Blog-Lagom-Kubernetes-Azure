package be.reaktika.cookie.impl

import be.reaktika.cookie.impl.state.Cookie
import be.reaktika.cookie.impl.state.Cookie.{Accepted, BadRequest, CookieCreated}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object CookieServiceSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] =
    Seq(
      // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)
      JsonSerializer[Cookie],
      JsonSerializer[CookieCreated],
      // the replies use play-json as well
      JsonSerializer[Accepted],
      JsonSerializer[BadRequest],
    )
}
