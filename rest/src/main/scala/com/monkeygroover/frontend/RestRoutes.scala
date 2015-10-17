package com.monkeygroover.frontend

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.monkeygroover.commands._
import com.monkeygroover.persistence.Record
import spray.json.DefaultJsonProtocol._   //don't remove this even if intelliJ tries to..

import scala.concurrent.duration._

/**
 * Created by monkeygroover on 09/10/15.
 */
trait RestRoutes extends Directives {
  implicit def system: ActorSystem
  implicit def materializer: Materializer
  implicit def shardRegion: ActorRef

  implicit val timeout = Timeout(10.seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val route =
    post {
      path("customer" / Segment) { customerId =>
        entity(as[PartialRecord]) { record =>
          val futureRes = shardRegion ? Add(s"customer-$customerId", record) map {
            case CommandResult.Ok => HttpResponse(StatusCodes.OK)
            case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
          }

          onSuccess(futureRes) { result =>
            complete(result)
          }
        }
      }
    } ~
      get {
        path("customer" / Segment) { customerId =>
          val futureRes = (shardRegion ? Get(s"customer-$customerId")).mapTo[List[Record]]

          onSuccess(futureRes) { result =>
            complete(result)
          }
        }
      } ~
      patch {
        path("customer" / Segment / Segment) { (customerId, uuid) =>
          entity(as[UpdateRecord]) { update =>
            val futureRes = shardRegion ? Update(s"customer-$customerId", uuid, update) map {
              case CommandResult.Ok => HttpResponse(StatusCodes.OK)
              case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
            }

            onSuccess(futureRes) { result =>
              complete(result)
            }
          }
        }
      } ~
      post {
        path("customer" / Segment / Segment) { (customerId, uuid) =>
          val futureRes = shardRegion ? Delete(s"customer-$customerId", uuid) map {
            case CommandResult.Ok => HttpResponse(StatusCodes.OK)
            case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
          }

          onSuccess(futureRes) { result =>
            complete(result)
          }
        }
      }
}
