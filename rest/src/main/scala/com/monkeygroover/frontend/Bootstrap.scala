package com.monkeygroover.frontend

import akka.actor.ActorSystem
import akka.cluster.sharding.ClusterSharding
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.monkeygroover.service.CustomerService
import com.typesafe.config.ConfigFactory

/**
 * Created by monkeygroover on 09/10/15.
 */
object Bootstrap extends RestRoutes {
  val conf = ConfigFactory.load()

  implicit val system = ActorSystem("ClusterSystem", conf)
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  // register cluster sharding, but this node is proxy only (i.e. doesn't host actor instances, has a None props)
  val shardRegion = ClusterSharding(system).startProxy(
    typeName = CustomerService.Shard.name,
    None,
    CustomerService.Shard.entityIdExtractor,
    CustomerService.Shard.shardIdExtractor
  )

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(handler = route, interface = "localhost", port = 8080)
  }
}
