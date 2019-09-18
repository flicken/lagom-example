package net.flicken.swingtime.impl

import net.flicken.swingtime.api
import net.flicken.swingtime.api.SwingtimeService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

/**
  * Implementation of the SwingtimeService.
  */
class SwingtimeServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends SwingtimeService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the swingtime entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SwingtimeEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the swingtime entity for the given ID.
    val ref = persistentEntityRegistry.refFor[SwingtimeEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }


  override def greetingsTopic(): Topic[api.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(SwingtimeEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[SwingtimeEvent]): api.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => api.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
