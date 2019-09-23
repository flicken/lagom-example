package net.flicken.projects.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import net.flicken.projects.api.ProjectsService
import net.flicken.projects.api
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class ProjectsServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new ProjectsApplication(ctx) with LocalServiceLocator
  }

  val client: ProjectsService = server.serviceClient.implement[ProjectsService]

  override protected def afterAll(): Unit = server.stop()

  "project service" should {
    "allow adding a project" in {
      client.addProject().invoke(api.AddProject("a name")).map { answer =>
        answer.name shouldBe "a name"
      }
    }

    "allow getting a project" in {
      client.addProject()
        .invoke(api.AddProject("a name"))
          .flatMap { addProject =>
            client.getProject(addProject.projectId).invoke()
          }.map { answer =>
          answer.name shouldBe "a name"
      }
    }
  }
}
