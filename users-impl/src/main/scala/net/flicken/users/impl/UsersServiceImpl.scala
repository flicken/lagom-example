package net.flicken.users.impl

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import net.flicken.users.api
import net.flicken.users.api.{UserMessage, UsersService}

import scala.concurrent.ExecutionContext

class UsersServiceImpl(persistentEntityRegistry: PersistentEntityRegistry)(implicit ec: ExecutionContext) extends UsersService {

  override def addUser(): ServiceCall[api.AddUser, UserMessage] = ServiceCall { addUser =>
    val id = UUID.randomUUID().toString
    val ref = persistentEntityRegistry.refFor[UserEntity](id)

    ref.ask(AddUser(addUser.name))
      .map(_ => api.UserMessage(id, addUser.name))
  }

  override def getUser(userId: String): ServiceCall[NotUsed, UserMessage] = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[UserEntity](userId)

    ref.ask(GetUser(userId))
      .map(user => api.UserMessage(userId, user.name))
  }

  override def updateUser(UsersId: String): ServiceCall[UserMessage, UserMessage] = ???

  override def deleteUser(UsersId: String): ServiceCall[NotUsed, UserMessage] = ???
}

