package com.monkeygroover.frontend

import akka.actor.{ActorSystem, Props}
import akka.cluster.sharding.ClusterSharding
import akka.pattern.ask
import akka.util.Timeout
import com.monkeygroover.backend.CustomerService
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object FrontendMain extends App {

  val conf = ConfigFactory.load()
  
  val system = ActorSystem("ClusterSystem", conf)

  // register cluster sharding, but this node is proxy only (i.e. doesn't host actor instances, has a None props)
  ClusterSharding(system).startProxy(
    typeName = CustomerService.Shard.name,
    None,
    CustomerService.Shard.entityIdExtractor,
    CustomerService.Shard.shardIdExtractor
  )

  val frontend = system.actorOf(Props[FrontendCustomer], "frontend")

  Thread.sleep(5000)
  
  implicit val timeout = Timeout(30.seconds)

  import com.monkeygroover.backend._
  
  var ackCount = 0;
   
  for(x <- 1 to 50) {
    
    val customerId = "customer-" + x % 10000
    val customerRecordSubmission = Record(s"blah $x", "blah blah!")
    
    println(s"about to submit $customerRecordSubmission to $customerId")
    
    import scala.concurrent.ExecutionContext.Implicits.global
    frontend ? AddRecord(customerId, customerRecordSubmission) map {
      case Frontend.Ok => ackCount += 1;
    }
 
    Thread.sleep(2)
  }
   
  System.in.read
  
//  import system.dispatcher
//  //gather counts
//
//   val futList = for(x <- 0 to 9) yield {ask(frontend, GetCount("test" + x % 10)).mapTo[Int]}
//
//   val result = Await.result(Future.sequence(futList), 30.seconds);
//
//   val gatheredCounts = result.reduce(_+_)
//
//   println(s"counted $ackCount, gathered $gatheredCounts")
}
  