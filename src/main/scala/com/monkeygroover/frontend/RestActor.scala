package com.monkeygroover.frontend

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.monkeygroover.backend.{AddRecord, CommandResult, GetRecords, Record}
import spray.http.{HttpEntity, HttpResponse, StatusCodes}
import spray.routing.HttpServiceActor
import spray.httpx.SprayJsonSupport._

import scala.concurrent.duration._

/**
 * Created by monkeygroover on 09/10/15.
 */
class RestActor(shardRegion: ActorRef) extends HttpServiceActor {
  def receive = runRoute(route)

  implicit val timeout = Timeout(50.seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  val route = path("consumer" / Segment) { id =>
    post {
      entity(as[Record]) { record =>
        val futureRes = shardRegion ? AddRecord(s"customer-$id", record) map {
          case CommandResult.Ok => HttpResponse(StatusCodes.OK)
          case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
        }

        onSuccess(futureRes) { result =>
          complete(result)
        }
      }
    } ~
      get {
        val futureRes = shardRegion ? GetRecords(s"customer-$id") map {
          case x => HttpResponse(StatusCodes.OK, HttpEntity(x.toString))
        }

        onComplete(futureRes) { result =>
          complete(result)
        }
      }
  }
}
