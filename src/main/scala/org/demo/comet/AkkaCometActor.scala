package org.demo.comet

import akka.actor.Actor
import akka.actor.Actor.{remote,actorOf}
import akka.actor.ActorRef 
import net.liftweb.http.{CometActor,SHtml}
import org.demo.akka.rabbitbridge.{DemoMessage,ListenerUpdate}

trait AkkaCometActor extends CometActor {
  implicit val akkaProxy: Option[ActorRef] = Some(Actor.actorOf(new Actor{
    protected def receive = {
      case a => AkkaCometActor.this ! a  
    }
  }))
  override def localSetup {
    super.localSetup
    akkaProxy.foreach(actorRef => 
      {
    	actorRef.start
    	remote.register("AkkaCometActor",actorRef)
    	val transformer = remote.actorFor("org.demo.akka.rabbitbridge.TransformerQueueListener", "localhost", 2552)
    	transformer ! ListenerUpdate("join",actorRef)
      })
  }
  override def localShutdown {
    super.localShutdown
    akkaProxy.foreach(actorRef => 
      {
    	actorRef.stop
    	remote.unregister(actorRef)
    	val transformer = remote.actorFor("org.demo.actor.TransformerQueueListener", "localhost", 2552)
    	transformer ! ListenerUpdate("quit",actorRef)
      })        
  }
}