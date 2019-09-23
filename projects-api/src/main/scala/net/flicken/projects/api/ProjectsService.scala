package net.flicken.projects.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object ProjectsService  {
  val TOPIC_NAME = "projects"
}

trait ProjectsService extends Service {
  def projects(): ServiceCall[NotUsed, Seq[ProjectMessage]]
  def projectTopic(): Topic[ProjectMessage]

  /**
    * Example: curl -H "Content-Type: application/json" -X POST -d '{"message":
    * "Hi"}' http://localhost:9000/api/hello/Alice
    */
  def addProject(): ServiceCall[AddProject, ProjectMessage]
  def getProject(projectId: String): ServiceCall[NotUsed, ProjectMessage]
  def updateProject(projectId: String): ServiceCall[ProjectMessage, ProjectMessage]
  def deleteProject(projectId: String): ServiceCall[NotUsed, ProjectMessage]
  def listProjects(): ServiceCall[NotUsed, Seq[ProjectMessage]]

  def addTask(projectId: String): ServiceCall[AddTask, TaskMessage]
  def getTask(projectId: String, taskId: String): ServiceCall[NotUsed, TaskMessage]
  def updateTask(projectId: String, taskId: String): ServiceCall[TaskMessage, TaskMessage]
  def deleteTask(projectId: String, taskId: String): ServiceCall[NotUsed, Done]
  def listTasks(projectId: String): ServiceCall[NotUsed, Seq[TaskMessage]]

  override final def descriptor: Descriptor = {
    import Service._
    // @formatter:off
    named("projects")
      .withCalls(
        restCall(Method.GET, "/api/projects/", listProjects),
        restCall(Method.POST, "/api/projects/", addProject _),
        restCall(Method.GET, "/api/projects/:projectId", getProject _),
        restCall(Method.PATCH, "/api/projects/:projectId", updateProject _),
        restCall(Method.DELETE, "/api/projects/:projectId", deleteProject _),

        restCall(Method.GET, "/api/projects/:projectId/tasks", listTasks _),
        restCall(Method.POST, "/api/projects/:projectId/tasks", addTask _),
        restCall(Method.GET, "/api/projects/:projectId/tasks/:taskId", getTask _),
        restCall(Method.PATCH, "/api/projects/:projectId/tasks/:taskId", updateTask _),
        restCall(Method.DELETE, "/api/projects/:projectId/tasks/:taskId", deleteTask _)
      )
      /*
      .withTopics(
        topic(ProjectService.TOPIC_NAME, projectTopic _)
          .addProperty(
            KafkaProperties.partitionKeyStrategy,
            PartitionKeyStrategy[ProjectServiceMessage](_.projectId)
          )
      )
       */
      .withAutoAcl(true)
    // @formatter:on
  }
}

abstract sealed trait ProjectServiceMessage {
  def projectId: String
}

case class ProjectMessage(projectId: String, name: String, tasks: Seq[TaskMessage]) extends ProjectServiceMessage

object ProjectMessage {
  implicit val format: Format[ProjectMessage] = Json.format[ProjectMessage]
}

case class ProjectChanged(id: String, name: String)

object ProjectChanged {
  implicit val format: Format[ProjectChanged] = Json.format[ProjectChanged]
}

case class AddProject(name: String)

object AddProject {
  implicit val format: Format[AddProject] = Json.format[AddProject]
}

case class TaskMessage(description: String)

object TaskMessage {
  implicit val format: Format[TaskMessage] = Json.format[TaskMessage]
}

case class AddTask(description: String)

object AddTask {
  implicit val format: Format[AddTask] = Json.format[AddTask]
}
