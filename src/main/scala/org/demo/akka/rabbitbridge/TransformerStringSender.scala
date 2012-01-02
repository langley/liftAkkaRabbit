package org.demo.akka.rabbitbridge

import com.rabbitmq.client.{ConnectionFactory,ConnectionParameters}
import net.liftweb.amqp.{AMQPSender,StringAMQPSender,AMQPMessage}
import net.liftweb.amqp.AMQPMessage

/**
 * By default, lift-amqp comes with a basic class serilizer for string AMQP 
 * messages, if you want to serilize / deserilize (send) other objects over 
 * the wire then implementing your own AMQPSender is relitivly easy.
 * 
 * @see net.liftweb.amqp.StringAMQPSender
 */
object TransformerStringSender {
  val params = new ConnectionParameters
  // All of the params, exchanges, and queues are all just example data.
  params.setUsername("guest")
  params.setPassword("guest")
  params.setVirtualHost("/")
  params.setRequestedHeartbeat(0)
  val factory = new ConnectionFactory(params)
  // Create a new instance of the string sender.
  // This sender will send messages to the "mult" exchange with a 
  // routing key of "routeroute"
  val amqp = new StringAMQPSender(factory, "localhost", 5672, "mult", "relay.messages")
  
  /**
   * Salute the rabbit!
   */
  def salute = amqp ! AMQPMessage("Hey there @ " + new java.util.Date())
  
  def send(message: String) = { 
    amqp ! AMQPMessage(message)
  }
}

