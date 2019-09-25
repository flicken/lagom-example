package net.flicken.users.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import net.flicken.users.api.UsersService
import org.pac4j.core.client.Client
import org.pac4j.core.config.Config
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.DirectBasicAuthClient
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator
import play.api.libs.ws.ahc.AhcWSComponents

class UsersApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new UsersApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new UsersApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[UsersService])
}

abstract class UsersApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[UsersService](wire[UsersServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = UserSerializerRegistry

  persistentEntityRegistry.register(wire[UserEntity])

  lazy val client: Client[UsernamePasswordCredentials, CommonProfile] = {
    val basicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator())
    basicAuthClient.addAuthorizationGenerator((ctx: WebContext, profile: CommonProfile) => {
      if (profile.getUsername == "user") {
        profile.addRole("user")
      } else if (profile.getUsername == "admin") {
        profile.addRole("admin")
      }
      profile
    })
    basicAuthClient
  }

  lazy val serviceConfig: Config = {
    val config = new Config(client)

    config.getClients.setDefaultSecurityClients(client.getName)
    config
  }
}
