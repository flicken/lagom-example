package net.flicken.projects.impl

import java.util.concurrent.TimeUnit

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._
import net.flicken.projects.api.ProjectsService
import net.flicken.users.api.UsersService
import org.pac4j.core.client.Client
import org.pac4j.core.config.Config
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.DirectBasicAuthClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ProjectApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ProjectsApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ProjectsApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ProjectsService])
}

abstract class ProjectsApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[ProjectsService](wire[ProjectsServiceImpl])

  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = ProjectSerializerRegistry

  persistentEntityRegistry.register(wire[ProjectEntity])

  lazy val client: Client[UsernamePasswordCredentials, CommonProfile] = {
    val basicAuthClient = new DirectBasicAuthClient(new Authenticator[UsernamePasswordCredentials] {
      override def validate(credentials: UsernamePasswordCredentials, context: WebContext): Unit = {
        val request = usersService.auth()
          .handleRequestHeader(_.addHeader("Authorization", context.getRequestHeader("Authorization")))
          .invoke()
      /*
        This is blocking, which isn't good practice.
        This seems like a downside of using the pac4j library.
        TODO Investigate a different way to handle authentication that isn't blocking
       */
        val result = Await.result(request, Duration(1, TimeUnit.SECONDS))

        val profile = new CommonProfile()
        profile.setId(result.id)
        result.attributes.roles.foreach(profile.addRole)
        credentials.setUserProfile(profile)
      }
    })

    basicAuthClient
  }

  lazy val usersService: UsersService = serviceClient.implement[UsersService]

  lazy val serviceConfig: Config = {
    val config = new Config(client)

    config.getClients.setDefaultSecurityClients(client.getName)
    config
  }
}
