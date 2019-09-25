package net.flicken.projects.impl

import java.util.Base64

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
import com.lightbend.lagom.scaladsl.server.{LocalServiceLocator, ServerServiceCall}
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import net.flicken.projects.api.ProjectsService
import net.flicken.projects.api
import net.flicken.users.api.{AddUser, RestProfileAttributes, RestProfileMessage, UserMessage, UsersService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.Future

class ProjectsServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new ProjectsApplication(ctx) with LocalServiceLocator {
      override lazy val usersService = new UsersService {
        override def addUser(): ServiceCall[AddUser, UserMessage] = ???

        override def getUser(UsersId: String): ServiceCall[NotUsed, UserMessage] = ???

        override def updateUser(UsersId: String): ServiceCall[UserMessage, UserMessage] = ???

        override def deleteUser(UsersId: String): ServiceCall[NotUsed, UserMessage] = ???

        override def auth(): ServiceCall[NotUsed, RestProfileMessage] = ServerServiceCall { _ =>
          Future.successful(RestProfileMessage("user", RestProfileAttributes("", Seq("user"))))
        }
      }
    }
  }

  val client: ProjectsService = server.serviceClient.implement[ProjectsService]

  override protected def afterAll(): Unit = server.stop()

  "project service" should {
    "allow adding a project" in {
      client.addProject()
        .handleRequestHeader(withBasicAuth("user", "user"))
        .invoke(api.AddProject("a name")).map { answer =>
        answer.name shouldBe "a name"
      }
    }

    "allow getting a project" in {
      client.addProject()
        .handleRequestHeader(withBasicAuth("user", "user"))
        .invoke(api.AddProject("a name"))
          .flatMap { addProject =>
            client
              .getProject(addProject.projectId)
              .handleRequestHeader(withBasicAuth("user", "user"))
              .invoke()
          }.map { answer =>
            answer.name shouldBe "a name"
          }
    }
  }

  private def withBasicAuth(username: String, password: String): RequestHeader => RequestHeader = {
    rh =>
      val encoded = Base64.getEncoder.encodeToString(s"$username:$password".getBytes())
      rh.addHeader("Authorization", "Basic " + encoded)
  }
}
