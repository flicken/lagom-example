package net.flicken.swingtimestream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import net.flicken.swingtimestream.api.SwingtimeStreamService
import net.flicken.swingtime.api.SwingtimeService
import com.softwaremill.macwire._

class SwingtimeStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SwingtimeStreamApplication(context) {
      override def serviceLocator: NoServiceLocator.type = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SwingtimeStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SwingtimeStreamService])
}

abstract class SwingtimeStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[SwingtimeStreamService](wire[SwingtimeStreamServiceImpl])

  // Bind the SwingtimeService client
  lazy val swingtimeService: SwingtimeService = serviceClient.implement[SwingtimeService]
}
