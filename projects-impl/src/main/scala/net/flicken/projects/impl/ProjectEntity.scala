package net.flicken.projects.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

class ProjectEntity extends PersistentEntity {

  override type Command = ProjectCommand[_]
  override type Event = ProjectEvent
  override type State = ProjectState

  override def initialState: ProjectState = ProjectState("", Seq())

  override def behavior: Behavior = {
    case ProjectState(_, _) => Actions().onCommand[AddProject, ProjectState] {
      case (AddProject(name), ctx, state) =>
        ctx.thenPersist(
          ProjectAdded(name)
        ){ _ =>
          ctx.reply(initialState.copy(name = name))
        }
    }.onCommand[AddTask, Done] {
      case (AddTask(description), ctx, state) =>
        ctx.thenPersist(
          TaskAdded(description)
        ) { _ =>
          ctx.reply(Done)
        }
    }.onReadOnlyCommand[GetProject, ProjectState] {
      case (GetProject(_), ctx, state) =>
        ctx.reply(state)
    }.onEvent(eventHandlers)
  }

  private def eventHandlers: EventHandler = {
    case (ProjectAdded(name), state) =>
      ProjectState(name, Seq())
    case (TaskAdded(description), state) =>
      state.copy(tasks = state.tasks :+ TaskState(description))
  }
}

case class ProjectState(name: String, tasks: Seq[TaskState])

object ProjectState {
  implicit val format: Format[ProjectState] = Json.format
}

case class TaskState(description: String)

object TaskState {
  implicit val format: Format[TaskState] = Json.format
}

sealed trait ProjectEvent extends AggregateEvent[ProjectEvent] {
  def aggregateTag: AggregateEventTag[ProjectEvent] = ProjectEvent.Tag
}

object ProjectEvent {
  val Tag: AggregateEventTag[ProjectEvent] = AggregateEventTag[ProjectEvent]
}

case class ProjectAdded(name: String) extends ProjectEvent

object ProjectAdded {
  implicit val format: Format[ProjectAdded] = Json.format
}

case class TaskAdded(description: String) extends ProjectEvent

object TaskAdded {
  implicit val format: Format[TaskAdded] = Json.format
}

sealed trait ProjectCommand[R] extends ReplyType[R]

case class AddProject(description: String) extends ProjectCommand[ProjectState]

object AddProject {
  implicit val format: Format[AddProject] = Json.format
}

case class AddTask(description: String) extends ProjectCommand[Done]

object AddTask {
  implicit val format: Format[AddTask] = Json.format
}

case class GetProject(projectId: String) extends ProjectCommand[ProjectState]

object GetProject {
  implicit val format: Format[GetProject] = Json.format
}

case class ProjectException(message: String) extends RuntimeException(message)

object ProjectException {
  implicit val format: Format[ProjectException] = Json.format[ProjectException]
}


object ProjectSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[AddProject],
    JsonSerializer[ProjectAdded],
    JsonSerializer[AddTask],
    JsonSerializer[TaskAdded],
    JsonSerializer[GetProject],
    JsonSerializer[ProjectState],
    JsonSerializer[TaskState],
    JsonSerializer[ProjectException]
  )
}
