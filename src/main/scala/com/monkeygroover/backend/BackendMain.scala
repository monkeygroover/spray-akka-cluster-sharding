package com.monkeygroover.backend

import akka.actor.ActorSystem
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}
import com.typesafe.config.ConfigFactory

object BackendMain extends App {
  val role = "customer-data"

  val conf = ConfigFactory.parseString(s"akka.cluster.roles=[$role]").
    withFallback(ConfigFactory.load())

  val system = ActorSystem("ClusterSystem", conf)

  //set up cluster sharding
  ClusterSharding(system).start(
      typeName = CustomerService.Shard.name,
      entityProps = CustomerService.props,
      settings = ClusterShardingSettings(system),
      extractEntityId = CustomerService.Shard.entityIdExtractor,
      extractShardId = CustomerService.Shard.shardIdExtractor
    )
}