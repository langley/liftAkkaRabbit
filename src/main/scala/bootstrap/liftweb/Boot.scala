package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._

// import code.model._
import org.demo.model._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    if (!DB.jndiJdbcConnAvailable_?) {
      val vendor = 
	new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
			     Props.get("db.url") openOr 
			     "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
			     Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _, User)

    // where to search snippet
    // LiftRules.addToPackages("code") default LiftWeb 
    LiftRules.addToPackages("org.demo") // for our LiftAkkaRabbitMQ demo 

    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Home") / "index" >> User.AddUserMenusAfter, // the simple way to declare a menu
      Menu("Game Display") / "gameDisplay",
      Menu("Messenger Display") / "messengerDisplay", 
      Menu("Akka Calculator") / "akka-calculator",
      // more complex because this menu allows anything in the
      // /static path to be visible
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")))

    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) =>
      new Html5Properties(r.userAgent))    

    // Make a transaction span the whole HTTP request
    // S.addAround(DB.buildLoanWrapper)
    
    // Akka Supervisors
    import akka.actor.Actor
    import akka.actor.Actor.{remote,actorOf}
    import akka.actor.Supervisor
    import akka.config.Supervision.{SupervisorConfig,OneForOneStrategy,Supervise,Permanent}
    import org.demo.actor.{HelloWorldActor,IntTransformer}
    
    /**
     * Boot the akka remote actor service
     * You can disable this during development as its "sodding" 
     * annoying to keep having the ports occupied!
     */
     remote.start("localhost", 2552)
     remote.register("hello-service", actorOf[HelloWorldActor])
    
    LiftRules.unloadHooks.append(() => {
      Actor.registry.shutdownAll
    }) 
    
    /**
     * Configure the supervisor heirarchy and determine the 
     * respective cases of failure.
     */
    Supervisor(
      SupervisorConfig(
        OneForOneStrategy(List(classOf[Throwable]), 3, 1000),
        Supervise(
          actorOf[org.demo.actor.IntTransformer],
          Permanent,
          true) ::
        Supervise(
          actorOf[org.demo.comet.Calculator],
          Permanent,
          true) ::
        Supervise(
          actorOf[org.demo.comet.MessengerActor],
          Permanent,
          true) :: 
        Supervise(
          actorOf[org.demo.akka.rabbitbridge.TransformerQueueListener],
          Permanent,
          true) ::           
        Nil))
  }
}
