package com.monkeygroover.seed

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object Bootstrap extends App {
  val conf = ConfigFactory.load()

  val system = ActorSystem("ClusterSystem", conf)
}