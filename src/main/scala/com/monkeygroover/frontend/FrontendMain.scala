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
  val customerRegion = ClusterSharding(system).startProxy(
    typeName = CustomerService.Shard.name,
    None,
    CustomerService.Shard.entityIdExtractor,
    CustomerService.Shard.shardIdExtractor
  )

  Thread.sleep(5000)
  
  implicit val timeout = Timeout(30.seconds)

  import com.monkeygroover.backend._
  
  var ackCount = 0;
   
  for(x <- 1 to 100000) {
    
    val customerId = "customer-" + x % 100
    val customerRecordSubmission = PartialRecord(s"blah $x", "blah blah!")
    
    println(s"about to submit $customerRecordSubmission to $customerId")
    
    import scala.concurrent.ExecutionContext.Implicits.global
    customerRegion ? AddRecord(customerId, customerRecordSubmission) map {
      case CommandResult.Ok => ackCount += 1
      case CommandResult.Rejected => ackCount += 1
    }
 
    Thread.sleep(1)
  }
   
  System.in.read

}
  