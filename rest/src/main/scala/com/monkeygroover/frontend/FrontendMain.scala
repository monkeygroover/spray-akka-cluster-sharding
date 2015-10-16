package com.monkeygroover.frontend

import akka.actor.ActorSystem
import akka.cluster.sharding.ClusterSharding
import akka.pattern.ask
import akka.util.Timeout
import com.monkeygroover.commands.{Add, CommandResult, PartialRecord}
import com.monkeygroover.service.CustomerService
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

  var ackCount = 0;
   
  for(x <- 1 to 100;
      y <- 1 to 10) {

    val customerId = "customer-" + x
    val customerRecordSubmission = PartialRecord(s"blah $y", "blah blah", "blah blah blah!")
    
    println(s"about to submit $customerRecordSubmission to $customerId")
    
    import scala.concurrent.ExecutionContext.Implicits.global
    customerRegion ? Add(customerId, customerRecordSubmission) map {
      case CommandResult.Ok => ackCount += 1
      case CommandResult.Rejected => ackCount += 1
    }
 
    Thread.sleep(1)
  }
   
  System.in.read

}
  