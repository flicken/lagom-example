package net.flicken.projects.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import net.flicken.users.api
import net.flicken.users.impl.{AddUser, GetUser, UserCommand, UserEntity, UserEvent, UserSerializerRegistry, UserState}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.immutable.Seq

class UserEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("UserEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(UserSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[UserCommand[_], UserEvent, UserState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new UserEntity(), "User-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "Project entity" should {
    "be able to add a user with name" in withTestDriver { driver =>
      val outcome = driver.run(AddUser("a name"))

      outcome.state shouldBe UserState("a name")
    }

    "be able to query state" in withTestDriver { driver =>
      val outcome = driver.run(
        AddUser("a name"),
        GetUser("")
      )

      val state = outcome.replies.seq.last
      state shouldBe UserState("a name")
    }

  }
}
