package com.monkeygroover.frontend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.monkeygroover.backend.{AddRecord, CommandResult, GetRecords, Record}
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

  val route = path("consumer" / Segment) { id =>
    post {
      entity(as[Record]) { record =>
        val futureRes = shardRegion ? AddRecord(s"customer-$id", record) map {
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
        val futureRes = (shardRegion ? GetRecords(s"customer-$id")).mapTo[List[Record]]

        onSuccess(futureRes) { result =>
          respondWithMediaType(`application/json`) {
            complete(result)
          }
        }
      }
  }
}
