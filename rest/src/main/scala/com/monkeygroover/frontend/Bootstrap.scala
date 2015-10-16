package com.monkeygroover.frontend

import akka.actor.{Props, ActorSystem}
import akka.cluster.sharding.ClusterSharding
import akka.io.IO
import com.monkeygroover.backend.CustomerService
import com.typesafe.config.ConfigFactory
import spray.can.Http

/**
 * Created by monkeygroover on 09/10/15.
 */
object Bootstrap extends App {
  val conf = ConfigFactory.load()

  implicit val system = ActorSystem("ClusterSystem", conf)

  // register cluster sharding, but this node is proxy only (i.e. doesn't host actor instances, has a None props)
  val customerRegion = ClusterSharding(system).startProxy(
    typeName = CustomerService.Shard.name,
    None,
    CustomerService.Shard.entityIdExtractor,
    CustomerService.Shard.shardIdExtractor
  )

  //create our service actor
  val service = system.actorOf(Props(classOf[RestActor], customerRegion), "my-service")

  //bind our actor to an HTTP port
  IO(Http) ! Http.Bind(service, interface = "localhost", port = 8080)
}
