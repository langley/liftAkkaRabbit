name := "liftAkkaRabbit"
 
seq(webSettings: _*)

checksums := Nil 

// If using JRebel, edit your sbt launch script or batch file and add before -jar:
//-noverify -javaagent:/path/to/jrebel.jar
//then uncomment the next line
//scanDirectories in Compile := Nil

resolvers += "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"

resolvers += "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots"

resolvers += "Akka Maven Repository" at "http://akka.io/repository"

libraryDependencies ++= {
  val liftVersion = "2.4-M5" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper"  % liftVersion % "compile->default",
	"net.liftweb" %% "lift-amqp" % liftVersion % "compile->default",
    //"net.liftweb" %% "lift-squeryl-record" % liftVersion % "compile->default",
    //"net.liftweb" %% "lift-widgets" % liftVersion % "compile->default"
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default",
	"se.scalablesolutions.akka" % "akka-actor" % "1.1" %  "compile->default",
	"se.scalablesolutions.akka" % "akka-remote" % "1.1" %  "compile->default"
	) 
}

// Customize any further dependencies as desired
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "7.3.1.v20110307" % "container,test", // For Jetty 7
  //"org.mortbay.jetty" % "jetty" % "6.1.26" % "container,test", // For Jetty 6	
  "com.novocode" % "junit-interface" % "0.7" % "test->default", //sbt's JUnit4 test interface
  "org.scala-tools.testing" %% "specs" % "1.6.9" % "test->default", // For specs.org tests
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "com.h2database" % "h2" % "1.2.147", // In-process database, useful for development systems
  //"mysql" % "mysql-connector-java" % "5.1.18", //uncomment this line for MySQL
  "ch.qos.logback" % "logback-classic" % "0.9.30" % "compile->default" // Logging
)
