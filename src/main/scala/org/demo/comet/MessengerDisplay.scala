package org.demo.comet

import akka.actor.Actor
import akka.actor.ActorRef
import scala.xml.{NodeSeq,Text}
import net.liftweb.common.{Box,Full,Empty}
import net.liftweb.util.Helpers._
import net.liftweb.http.{CometActor,SHtml, SessionVar}
import net.liftweb.http.js.JsCmds.{SetHtml,Noop}
import akka.actor.Actor.registry
import akka.dispatch.Future
import akka.AkkaException
import akka.actor.Actor.remote

import org.demo.akka.rabbitbridge.{DemoMessage,ListenerUpdate}

class MessengerDisplay extends AkkaCometActor {
  private var inputMessage = "unset"
  object relayedMessages extends SessionVar[List[DemoMessage]](Nil)
  
//  override def localSetup {
//	super.localSetup
//	val transformer = remote.actorFor("transformer", "localhost", 2552)
//	// transformer ! ListenerUpdate("join", this.asInstanceOf[AkkaCometActor])
//	transformer ! this
//  } 
  
//  override def localShutdown() { 
//    val transformer = remote.actorFor("transformer", "localhost", 2552)
//    // transformer ! ListenerUpdate("quit", this.asInstanceOf[AkkaCometActor])
//    transformer ! this
//    super.localShutdown()
//  }
  
  def messageInput(f: String => Any) = 
    SHtml.text("enter a message", input => f(input))
  
  def render = 
    "#messageInput" #> messageInput(inputMessage = _) &
    "#messages" #> {
    	relayedMessages.get.flatMap( 
			  			message => Box(".msg" #> Text(message.msg)))
    }	& 
    "type=submit" #> SHtml.ajaxSubmit("Submit", () => {
      println(">>>>> >>>> >>>> sending: " + inputMessage )
      registry.actorFor[MessengerActor].map {
        _ ! DemoMessage(inputMessage, new java.util.Date)
      }
      Noop
    }) andThen SHtml.makeFormsAjax
  
  // Comet actor message loop 
  override def mediumPriority = {
    case message: DemoMessage => 
      println(">>>> >>>> >>>> received: " + message.toString)
      relayedMessages.set(message :: relayedMessages.get) 
      val msgDisplay = relayedMessages.get.map(_.msg).mkString(", ")
      println(">>>> >>>> >>>> relayedMessages: " + msgDisplay)
      // partialUpdate(SetHtml("messages", Text(msgDisplay)))
      reRender(false) // This means to render, but not the whole page

  }
}

class MessengerActor extends Actor {
  // Akka actor message loop
  def receive = {
    case demoMsg@DemoMessage(msg, date) => {
      val result = DemoMessage(msg + " has been relayed!", new java.util.Date())
      self.reply(result) // replying to the Comet Actor trait
    }
  }
}


