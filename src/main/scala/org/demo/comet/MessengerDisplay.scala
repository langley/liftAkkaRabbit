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
import org.demo.akka.rabbitbridge.TransformerStringSender

class MessengerDisplay extends AkkaCometActor {
  private var inputMessage = "unset"
  object relayedMessages extends SessionVar[List[DemoMessage]](Nil)

  // --------------------------------------------------------------------------
  // Uses css selectors to generate the form and display the mesages  
  def render = { 
    "#messageInput" #> messageInput(inputMessage = _) &
    "#messages" #> {
    	relayedMessages.get.flatMap( 
			  			message => Box(".msg" #> Text(message.msg)))
    }	& 
    "type=submit" #> SHtml.ajaxSubmit("Submit", () => {
        TransformerStringSender.send(inputMessage)
      Noop
    }) andThen SHtml.makeFormsAjax
  }
  
  // --------------------------------------------------------------------------    
  // Comet actor message loop 
  override def mediumPriority = {
    case message: DemoMessage => 
      relayedMessages.set(message :: relayedMessages.get) 
      val msgDisplay = relayedMessages.get.map(_.msg).mkString(", ")
      reRender(false) // This means to render, but not the whole page
  }
    
  // --------------------------------------------------------------------------
  // Convenience method for generating input 
  def messageInput(f: String => Any) = {
    SHtml.text("enter a message", input => f(input))
  }

}


