package com.monkeygroover.backend

import akka.actor.ActorSystem
import akka.cluster.sharding.{ClusterShardingSettings, ClusterSharding}
import com.monkeygroover.service.CustomerService
import com.typesafe.config.ConfigFactory

object Bootstrap extends App {
  val conf = ConfigFactory.load()

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