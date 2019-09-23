package net.flicken.projects.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import net.flicken.users.api
import net.flicken.users.api.UsersService
import net.flicken.users.impl.UsersApplication
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class UsersServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra()
  ) { ctx =>
    new UsersApplication(ctx) with LocalServiceLocator
  }

  val client: UsersService = server.serviceClient.implement[UsersService]

  override protected def afterAll(): Unit = server.stop()

  "users service" should {
    "allow adding a user" in {
      client.addUser().invoke(api.AddUser("a name")).map { answer =>
        answer.name shouldBe "a name"
      }
    }

    "allow getting a user" in {
      client.addUser()
        .invoke(api.AddUser("a name"))
          .flatMap { addUser =>
            client.getUser(addUser.id).invoke()
          }.map { answer =>
          answer.name shouldBe "a name"
      }
    }
  }
}
