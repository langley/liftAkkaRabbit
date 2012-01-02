package org.demo.akka.rabbitbridge


import org.demo.comet.AkkaCometActor
import akka.actor.ActorRef

sealed case class DemoMessage(msg: String, date: java.util.Date) 
sealed case class ListenerUpdate(command: String, actor: ActorRef) 
