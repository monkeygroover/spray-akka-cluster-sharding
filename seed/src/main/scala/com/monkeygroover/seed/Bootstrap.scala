package com.monkeygroover.seed

import akka.actor.ActorSystem
import com.typesafe.config.{ConfigValueFactory, ConfigFactory}
import scala.collection.JavaConverters._

object Bootstrap extends App {

  // use configured ip or get host ip if available
  val ip = HostIP.load getOrElse "127.0.0.1"

  // add seed nodes to config
  //val seedNodesString = s"""akka.cluster.seed-nodes += "akka.tcp://CustomerService@${ip}:8000""""
 // val seedNodesString = s"""akka.cluster.seed-nodes += "akka.tcp://CustomerService@filth:8000""""
  val seedNodesStrings = List(s"akka.tcp://CustomerService@$ip:8000")
  val conf = ConfigFactory.load()
    .withValue("akka.remote.netty.tcp.hostname", ConfigValueFactory fromAnyRef ip)
    .withValue("akka.cluster.seedNodes", ConfigValueFactory fromIterable seedNodesStrings.asJavaCollection)
   .resolve

  val system = ActorSystem("ClusterSystem", conf)
}