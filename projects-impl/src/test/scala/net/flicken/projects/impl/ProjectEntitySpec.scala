package net.flicken.projects.impl

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.collection.immutable.Seq

class ProjectEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll {

  private val system = ActorSystem("ProjectEntitySpec",
    JsonSerializerRegistry.actorSystemSetupFor(ProjectSerializerRegistry))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private def withTestDriver(block: PersistentEntityTestDriver[ProjectCommand[_], ProjectEvent, ProjectState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new ProjectEntity(), "Project-1")
    block(driver)
    driver.getAllIssues should have size 0
  }

  "Project entity" should {
    "be able to add a project with name" in withTestDriver { driver =>
      val outcome = driver.run(AddProject("a name"))

      outcome.state shouldBe ProjectState("a name", Seq())
    }

    "be able to add task" in withTestDriver { driver =>
      val outcome = driver.run(
        AddProject("a name"),
        AddTask("a description")
      )

      outcome.state.tasks.map(_.description) should contain only "a description"
    }

    "be able to query state" in withTestDriver { driver =>
      val outcome = driver.run(
        AddProject("a name"),
        AddTask("a description"),
        GetProject("")
      )

      val projectState = outcome.replies.seq.last
      projectState shouldBe ProjectState("a name", Seq(TaskState("a description")))
    }

  }
}
