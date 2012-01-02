package org.demo.akka.rabbitbridge

import com.rabbitmq.client.{ConnectionFactory,ConnectionParameters,Channel}
import net.liftweb.amqp.{AMQPDispatcher,AMQPAddListener,AMQPMessage,SerializedConsumer}
import net.liftweb.actor.LiftActor
import akka.actor.Actor
import net.liftweb.http.CometActor
import org.demo.comet.AkkaCometActor

// --------------------------------------------------------------------------
// A RabbitMQ Listener that first transforms messages then relays them to 
// a list of interested actors 
class TransformerQueueListener()  extends Actor {
  val queueName = "relay"
  val params = new ConnectionParameters
  params.setUsername("guest")
  params.setPassword("guest")
  params.setVirtualHost("/")
  params.setRequestedHeartbeat(0)
  
  val factory = new ConnectionFactory(params)
  val amqp = new TransformerAMQPDispatcher[String](queueName, factory, "localhost", 5672)

  // --------------------------------------------------------------------------
  // The list of listeners that need to receive transformed messages
  var cometActors: List[CometActor] = Nil 
  
  // --------------------------------------------------------------------------  
  // Listener that does the actual work
  class StringListener extends LiftActor {
    override def messageHandler = {
      case msg@AMQPMessage(contents: String) =>
        import akka.actor.Actor._
        val cometActors = registry.actorsFor("AkkaCometActor", "localhost", 2552)
        cometActors.foreach(comet => {
          	println(">>>> +++ >>>> ++++ >>>> actor sent " + DemoMessage(">> " + msg.toString + " <<", new java.util.Date()))
          	comet ! DemoMessage(">> " + msg.toString + " <<", new java.util.Date())
            }
          )        
        println("TransformerQueueListener received: " + msg)
        msg 
      case fallThrough@_ => 
        println(">>>> >>>> >>> !!!! >>> Error, received: + " + fallThrough )
    }
  }
  val stringListener = new StringListener()
  amqp ! AMQPAddListener(stringListener)
  
  // --------------------------------------------------------------------------
  // Manage the list of listeners that need to be notified when each message comes in 
  def receive = { 
    case update@ListenerUpdate("join",_) =>
      cometActors = update.actor :: cometActors
    case update@ListenerUpdate("quit",_) =>
      cometActors = cometActors.filter(actor => actor != update.actor)
    case actor: AkkaCometActor => 
      if (cometActors.contains(actor)) cometActors = cometActors.filter(_ != actor) 
      else cometActors = actor :: cometActors
  }
}

// --------------------------------------------------------------------------
// Dispatcher for connecting to RabbitMQ
class TransformerAMQPDispatcher[T](queueName: String, factory: ConnectionFactory, host: String, port: Int)extends AMQPDispatcher[T](factory, host, port) { 
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
