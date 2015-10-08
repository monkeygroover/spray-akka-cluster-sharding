package com.monkeygroover.backend

import akka.actor.ActorSystem
import akka.contrib.pattern.ClusterSharding
import com.typesafe.config.ConfigFactory

object BackendMain extends App {
  val role = "customer-data"

  val conf = ConfigFactory.parseString(s"akka.cluster.roles=[$role]").
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", conf)

  //set up cluster sharding
  ClusterSharding(system).start(
      CustomerService.Shard.name,
      Some(CustomerService.props),
      CustomerService.Shard.idExtractor,
      CustomerService.Shard.shardResolver
    )
}