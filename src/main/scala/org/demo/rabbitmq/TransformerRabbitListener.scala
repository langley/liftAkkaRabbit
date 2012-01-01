package org.demo.rabbitmq

import com.rabbitmq.client.{ConnectionFactory,ConnectionParameters,Channel}
import net.liftweb.amqp.{AMQPDispatcher,AMQPAddListener,AMQPMessage,SerializedConsumer}
import net.liftweb.actor.LiftActor

class TransformerAMQPDispatcher[T](queueName: String, factory: ConnectionFactory, host: String, port: Int) extends AMQPDispatcher[T](factory, host, port) {
  override def configure(channel: Channel) {
    // Set up the exchange: Exchange, Type, IsDurable
    channel.exchangeDeclare("mult", "fanout", true)
    // Setup the queue: QueueName, IsDurable
    channel.queueDeclare(queueName, true)
    // Bind queue to exchange: QueueName, Exchange, RoutingKey
    channel.queueBind(queueName, "mult", "relay.*")
    // Use the short version of the basicConsume method for convenience.
    channel.basicConsume(queueName, false, new SerializedConsumer(channel, this))
  }
}

class TransformerQueueListener(queueName: String) {
  val params = new ConnectionParameters
  params.setUsername("guest")
  params.setPassword("guest")
  params.setVirtualHost("/")
  params.setRequestedHeartbeat(0)
  
  val factory = new ConnectionFactory(params)
  val amqp = new TransformerAMQPDispatcher[String](queueName, factory, "localhost", 5672)
  
  // Example Listener that just prints the String it receives.
  class StringListener extends LiftActor {
    override def messageHandler = {
      case msg@AMQPMessage(contents: String) => 
        println("TransformerQueueListener received: " + msg)
        msg
    }
  }
  val stringListener = new StringListener()
  amqp ! AMQPAddListener(stringListener)
}

