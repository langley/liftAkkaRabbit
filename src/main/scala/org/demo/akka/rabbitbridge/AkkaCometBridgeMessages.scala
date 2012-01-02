package org.demo.akka.rabbitbridge


import org.demo.comet.AkkaCometActor

sealed case class DemoMessage(msg: String, date: java.util.Date) 
sealed case class ListenerUpdate(command: String, actor: AkkaCometActor) 
