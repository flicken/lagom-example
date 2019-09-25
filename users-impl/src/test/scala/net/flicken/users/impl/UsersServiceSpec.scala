package net.flicken.projects.impl

import java.util.Base64

import com.lightbend.lagom.scaladsl.api.transport.RequestHeader
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
      client.addUser()
        .handleRequestHeader(withBasicAuth("admin", "admin"))
        .invoke(api.AddUser("a name"))
        .map { answer =>
          answer.name shouldBe "a name"
      }
    }

    "allow getting a user" in {
      client.addUser()
        .handleRequestHeader(withBasicAuth("admin", "admin"))
        .invoke(api.AddUser("a name"))
          .flatMap { addUser =>
            client.getUser(addUser.id)
              .handleRequestHeader(withBasicAuth("user", "user"))
              .invoke()
          }.map { answer =>
          answer.name shouldBe "a name"
      }
    }

    "allows authentication" in {
      client.addUser()
        .handleRequestHeader(withBasicAuth("admin", "admin"))
        .invoke(api.AddUser("a name"))
        .flatMap { _ =>
          client.auth()
            .handleRequestHeader(withBasicAuth("user", "user"))
            .invoke()
        }.map { answer =>
        answer.id shouldBe "user"
      }
    }
  }

  private def withBasicAuth(username: String, password: String): RequestHeader => RequestHeader = {
    rh: RequestHeader =>
      val encoded = Base64.getEncoder.encodeToString(s"$username:$password".getBytes())
      rh.addHeader("Authorization", "Basic " + encoded)
  }
}
