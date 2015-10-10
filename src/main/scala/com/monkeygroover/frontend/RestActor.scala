package com.monkeygroover.frontend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.monkeygroover.backend._
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

  val route = path("consumer" / Segment) { customerId =>
    post {
      entity(as[PartialRecord]) { record =>
        val futureRes = shardRegion ? AddRecord(s"customer-$customerId", record) map {
          case CommandResult.Ok => HttpResponse(StatusCodes.OK)
          case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
        }

        onSuccess(futureRes) { result =>
          respondWithMediaType(`application/json`) {
            complete(result)
          }
        }
      }
    } ~
      get {
        val futureRes = (shardRegion ? GetRecords(s"customer-$customerId")).mapTo[List[Record]]

        onSuccess(futureRes) { result =>
          respondWithMediaType(`application/json`) {
            complete(result)
          }
        }
      }
  } ~
    path("consumer" / Segment / Segment) { (customerId, uuid) =>
      post {
        val futureRes = shardRegion ? DeleteRecord(s"customer-$customerId", uuid) map {
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
