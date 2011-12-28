package org.demo.akka

import akka.actor.Actors
import org.demo.akka.GameActor

/**
 * This is just a way to create a singleton game engine
 * for use by our CometDisplay clients.
 */
object GameController {
  val game = Actors.actorOf(classOf[GameActor]).start()
}
