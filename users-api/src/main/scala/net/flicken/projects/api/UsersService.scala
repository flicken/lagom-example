package net.flicken.users.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object UsersService  {
  val TOPIC_NAME = "users"
}

trait UsersService extends Service {
  def addUser(): ServiceCall[AddUser, UserMessage]
  def getUser(UsersId: String): ServiceCall[NotUsed, UserMessage]
  def updateUser(UsersId: String): ServiceCall[UserMessage, UserMessage]
  def deleteUser(UsersId: String): ServiceCall[NotUsed, UserMessage]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("users")
      .withCalls(
        restCall(Method.POST, "/api/users/", addUser _),
        restCall(Method.GET, "/api/users/:userId", getUser _),
        restCall(Method.PATCH, "/api/users/:userId", updateUser _),
        restCall(Method.DELETE, "/api/users/:userId", deleteUser _),
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

abstract sealed trait UsersServiceMessage {
  def id: String
}

case class UserMessage(id: String, name: String) extends UsersServiceMessage

object UserMessage {
  implicit val format: Format[UserMessage] = Json.format[UserMessage]
}

case class UserChanged(id: String, name: String)

object UserChanged {
  implicit val format: Format[UserChanged] = Json.format[UserChanged]
}

case class AddUser(name: String)

object AddUser {
  implicit val format: Format[AddUser] = Json.format[AddUser]
}
