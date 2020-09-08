package be.reaktika.cookieanalytics.api

import com.lightbend.lagom.scaladsl.api.{ Descriptor, Service }

class CookieAnalyticsService extends Service {
  override def descriptor: Descriptor = {
    import Service._
    named("cookie-analytics")
  }
}
