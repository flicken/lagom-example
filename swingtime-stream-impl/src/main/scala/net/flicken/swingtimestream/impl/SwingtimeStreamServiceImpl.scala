package net.flicken.swingtimestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import net.flicken.swingtimestream.api.SwingtimeStreamService
import net.flicken.swingtime.api.SwingtimeService

import scala.concurrent.Future

/**
  * Implementation of the SwingtimeStreamService.
  */
class SwingtimeStreamServiceImpl(swingtimeService: SwingtimeService) extends SwingtimeStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(swingtimeService.hello(_).invoke()))
  }
}
