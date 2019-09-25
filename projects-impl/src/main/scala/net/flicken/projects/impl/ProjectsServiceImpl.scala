package net.flicken.projects.impl

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import net.flicken.projects.api.{ProjectMessage, ProjectsService, TaskMessage}
import net.flicken.projects.api
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer.requireAnyRole
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.lagom.scaladsl.SecuredService

import scala.concurrent.ExecutionContext

class ProjectsServiceImpl(persistentEntityRegistry: PersistentEntityRegistry,
                          override val securityConfig: Config)(implicit ec: ExecutionContext)
  extends ProjectsService
  with SecuredService {
  override def projects(): ServiceCall[NotUsed, Seq[ProjectMessage]] = ???

  override def projectTopic(): Topic[ProjectMessage] = ???

  override def addProject(): ServiceCall[api.AddProject, ProjectMessage] = authorize(requireAnyRole[CommonProfile]("user"), (profile: CommonProfile) =>
    ServerServiceCall { addProject: api.AddProject =>
      val id = UUID.randomUUID().toString
      val ref = persistentEntityRegistry.refFor[ProjectEntity](id)

      ref.ask(AddProject(addProject.name))
        .map(ps => api.ProjectMessage(id, ps.name, Seq()))
    }
  )

  override def getProject(projectId: String): ServiceCall[NotUsed, api.ProjectMessage] = authorize(requireAnyRole[CommonProfile]("user"), (profile: CommonProfile) =>
    ServerServiceCall { _: NotUsed =>
      val ref = persistentEntityRegistry.refFor[ProjectEntity](projectId)

      ref.ask(GetProject(projectId))
        .map(ps => api.ProjectMessage(projectId, ps.name, ps.tasks.map(convertToMessage _)))
    }
  )

  private def convertToMessage(task: TaskState): TaskMessage = {
    TaskMessage(task.description)
  }

  override def updateProject(projectId: String): ServiceCall[api.ProjectMessage, api.ProjectMessage] = ???

  override def deleteProject(projectId: String): ServiceCall[NotUsed, api.ProjectMessage] = ???

  override def listProjects(): ServiceCall[NotUsed, Seq[api.ProjectMessage]] = ???

  override def addTask(projectId: String): ServiceCall[api.AddTask, api.TaskMessage] = ???

  override def getTask(projectId: String, taskId: String): ServiceCall[NotUsed, api.TaskMessage] = ???

  override def updateTask(projectId: String, taskId: String): ServiceCall[api.TaskMessage, api.TaskMessage] = ???

  override def deleteTask(projectId: String, taskId: String): ServiceCall[NotUsed, Done] = ???

  override def listTasks(projectId: String): ServiceCall[NotUsed, Seq[api.TaskMessage]] = ???
}

