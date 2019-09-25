package net.flicken.users.impl

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import net.flicken.users.api
import net.flicken.users.api.{RestProfileAttributes, RestProfileMessage, UserMessage, UsersService}
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer.requireAnyRole
import org.pac4j.core.config.Config
import org.pac4j.core.exception.BadCredentialsException
import org.pac4j.core.profile.{AnonymousProfile, CommonProfile}
import org.pac4j.lagom.scaladsl.SecuredService

import scala.concurrent.{ExecutionContext, Future}

class UsersServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                       override val securityConfig: Config)
                      (implicit ec: ExecutionContext)
  extends UsersService
    with SecuredService {

  override def addUser(): ServiceCall[api.AddUser, UserMessage] =  authorize(requireAnyRole[CommonProfile]("admin"), (profile: CommonProfile) =>
     ServerServiceCall { addUser: api.AddUser =>
    val id = UUID.randomUUID().toString
    val ref = persistentEntityRegistry.refFor[UserEntity](id)

    ref.ask(AddUser(addUser.name))
      .map(_ => api.UserMessage(id, addUser.name))
  })

  override def getUser(userId: String): ServiceCall[NotUsed, UserMessage] = authorize(requireAnyRole[CommonProfile]("user"), (profile: CommonProfile) =>
    ServerServiceCall { _: NotUsed =>
      val ref = persistentEntityRegistry.refFor[UserEntity](userId)

      ref.ask(GetUser(userId))
        .map(user => api.UserMessage(userId, user.name))
    }
  )

  override def auth(): ServiceCall[NotUsed, RestProfileMessage] = authenticate { profile: CommonProfile =>
    ServerServiceCall { _ =>
      // Stubbing out, in order demonstrate async auth call from project service
      // In a real implementation, this would return a real user's profile, including roles
      if (profile.isInstanceOf[AnonymousProfile]) {
        throw new BadCredentialsException("Must not be anonymous")
      }
      Future.successful(RestProfileMessage(profile.getUsername,
        RestProfileAttributes(profile.getUsername, Seq(profile.getUsername))))
    }
  }


  override def updateUser(UsersId: String): ServiceCall[UserMessage, UserMessage] = ???
  override def deleteUser(UsersId: String): ServiceCall[NotUsed, UserMessage] = ???
}

