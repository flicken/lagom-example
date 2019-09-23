package net.flicken.users.impl

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

class UserEntity extends PersistentEntity {

  override type Command = UserCommand[_]
  override type Event = UserEvent
  override type State = UserState

  override def initialState: UserState = UserState("")

  override def behavior: Behavior = {
    case UserState( _) => Actions().onCommand[AddUser, UserState] {
      case (AddUser(name), ctx, state) =>
        ctx.thenPersist(
          UserAdded(name)
        ){ _ =>
          ctx.reply(initialState.copy(name = name))
        }
    }.onReadOnlyCommand[GetUser, UserState] {
      case (GetUser(_), ctx, state) =>
        ctx.reply(state)
    }.onEvent(eventHandlers)
  }

  private def eventHandlers: EventHandler = {
    case (UserAdded(name), state) =>
      UserState(name)
  }
}

case class UserState(name: String)

object UserState {
  implicit val format: Format[UserState] = Json.format
}

sealed trait UserEvent extends AggregateEvent[UserEvent] {
  def aggregateTag: AggregateEventTag[UserEvent] = UserEvent.Tag
}

object UserEvent {
  val Tag: AggregateEventTag[UserEvent] = AggregateEventTag[UserEvent]
}

case class UserAdded(name: String) extends UserEvent

object UserAdded {
  implicit val format: Format[UserAdded] = Json.format
}

sealed trait UserCommand[R] extends ReplyType[R]

case class AddUser(description: String) extends UserCommand[UserState]

object AddUser {
  implicit val format: Format[AddUser] = Json.format
}
case class GetUser(UserId: String) extends UserCommand[UserState]

object GetUser {
  implicit val format: Format[GetUser] = Json.format
}

case class UserException(message: String) extends RuntimeException(message)

object UserException {
  implicit val format: Format[UserException] = Json.format[UserException]
}

object UserSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AddUser],
    JsonSerializer[UserAdded],
    JsonSerializer[GetUser],
    JsonSerializer[UserState],
    JsonSerializer[UserException]
  )
}
