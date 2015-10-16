package com.monkeygroover.frontend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.monkeygroover.commands._
import com.monkeygroover.persistence.Record
import spray.http.MediaTypes.`application/json`
import spray.http.{HttpResponse, StatusCodes}
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._
import spray.routing.HttpServiceActor

import scala.concurrent.duration._

/**
 * Created by monkeygroover on 09/10/15.
 */
class RestActor(shardRegion: ActorRef) extends HttpServiceActor {
  def receive = runRoute(route)

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
            respondWithMediaType(`application/json`) {
              complete(result)
            }
          }
        }
      }
    } ~
      get {
        path("customer" / Segment) { customerId =>
          val futureRes = (shardRegion ? Get(s"customer-$customerId")).mapTo[List[Record]]

          onSuccess(futureRes) { result =>
            respondWithMediaType(`application/json`) {
              complete(result)
            }
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
              respondWithMediaType(`application/json`) {
                complete(result)
              }
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
            respondWithMediaType(`application/json`) {
              complete(result)
            }
          }
        }
      }
}
