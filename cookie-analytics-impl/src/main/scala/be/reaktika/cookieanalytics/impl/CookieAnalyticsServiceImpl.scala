package be.reaktika.cookieanalytics.impl

import akka.Done
import akka.stream.scaladsl.Flow
import be.reaktika.cookie.api.{ CookieEvents, CookieService }
import be.reaktika.cookieanalytics.api.CookieAnalyticsService
import com.typesafe.scalalogging.LazyLogging

class CookieAnalyticsServiceImpl(cookieService: CookieService) extends CookieAnalyticsService with LazyLogging {

  cookieService.cookieEventsOutTopic.subscribe
    .withGroupId("cookie-analytics")
    .atLeastOnce(Flow.fromFunction(cookieEvent => {
      cookieEvent match {
        case CookieEvents.CookieAdded(cookieName) =>
          logger.info(s"Cookie created: $cookieName")
      }
      Done
    }))
}
