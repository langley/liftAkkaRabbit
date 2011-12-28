package org.demo.akka.game

import akka.actor.Actors
import org.demo.akka.game.GameActor

/**
 * This is just a way to create a singleton game engine
 * for use by our CometDisplay clients.
 */
object GameController {
  val game = Actors.actorOf(classOf[GameActor]).start()
}
