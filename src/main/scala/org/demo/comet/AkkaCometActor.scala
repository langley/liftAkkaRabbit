package org.demo.comet

import akka.actor.Actor
import akka.actor.Actor.{remote,actorOf}
import akka.actor.ActorRef
import net.liftweb.http.{CometActor,SHtml}

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
      })
  }
  override def localShutdown {
    super.localShutdown
    akkaProxy.foreach(actorRef => 
      {
    	actorRef.stop
    	remote.unregister(actorRef)
      })        
  }
}