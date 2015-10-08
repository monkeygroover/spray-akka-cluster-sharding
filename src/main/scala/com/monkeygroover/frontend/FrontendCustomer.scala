package com.monkeygroover.frontend

import akka.actor.Actor
import akka.contrib.pattern.ClusterSharding
import akka.pattern._
import akka.util.Timeout
import com.monkeygroover.backend._

import scala.concurrent.duration._

object Frontend{
  case object Ok
  case object NotOk
}

class FrontendCustomer extends Actor {
  import Frontend._
  import context.dispatcher
  
  println("Frontend Customer")
  val customerProxy = ClusterSharding(context.system).shardRegion(CustomerService.Shard.name)
  
  def receive = {
    case command : CustomerCommands =>
      import Frontend._
      import com.monkeygroover.backend._
      implicit val timeout = Timeout(30.seconds)
      (customerProxy ? command) map {
        case CommandResult.Ok => Ok
        case c : Int => c
      } recover { 
        case x => println(x); NotOk 
        } pipeTo sender()
        
    case Ok => println("")
  }
}