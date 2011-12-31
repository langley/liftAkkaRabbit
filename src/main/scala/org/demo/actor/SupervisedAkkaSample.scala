package org.demo.actor

import akka.actor.Actor

class IntTransformer extends Actor {
  def receive = {
    case (in: String) => 
      println("IntTransformer Actor received: \"" + in + "\" and is trying to return it as an int ...")
      println("\t" + in.toInt)
  }
}

object IntTransformerRemoteCaller {
  import akka.actor.Actor.remote
  private val actor = remote.actorFor("org.demo.actor.IntTransformer", "localhost", 2552)
  def send(msg: String) = actor ! msg
}