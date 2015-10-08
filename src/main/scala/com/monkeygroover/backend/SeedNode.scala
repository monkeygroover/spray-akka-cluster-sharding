package com.monkeygroover.backend

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object SeedNode extends App {
    val role = "seed"

    val conf = ConfigFactory.parseString(s"akka.cluster.roles=[$role]").
    withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.port=2550")).
      withFallback(ConfigFactory.load())

    val system = ActorSystem("ClusterSystem", conf)
}