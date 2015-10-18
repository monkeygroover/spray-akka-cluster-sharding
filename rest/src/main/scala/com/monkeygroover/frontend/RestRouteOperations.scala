package com.monkeygroover.frontend

import akka.actor.{ActorRef, ActorSystem}
import akka.contrib.persistence.mongodb.{MongoReadJournal, ScalaDslMongoReadJournal}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.pattern.ask
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import com.monkeygroover.commands._
import com.monkeygroover.persistence.Record
import spray.json.DefaultJsonProtocol._   //don't remove this even if intelliJ tries to..

import scala.concurrent.duration._

/**
 * Created by monkeygroover on 09/10/15.
 */
class RestRouteOperations(shardRegion: ActorRef)(implicit system: ActorSystem, materializer: Materializer) extends RestRoutes {
  implicit val timeout = Timeout(10.seconds)

  import scala.concurrent.ExecutionContext.Implicits.global

  def addOperation(customerId: String, record: PartialRecord) = {
    val futureRes = shardRegion ? Add(s"customer-$customerId", record) map {
      case CommandResult.Ok => HttpResponse(StatusCodes.OK)
      case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
    }

    onSuccess(futureRes) { result =>
      complete(result)
    }
  }


  def getOperation(customerId: String) = {
    val futureRes = (shardRegion ? Get(s"customer-$customerId")).mapTo[List[Record]]

    onSuccess(futureRes) { result =>
      complete(result)
    }
  }


  lazy val readJournal =
    PersistenceQuery(system).readJournalFor[ScalaDslMongoReadJournal](MongoReadJournal.Identifier)

  def getHistoryOperation(customerId: String) = {
    // issue query to journal
    val source: Source[EventEnvelope, Unit] =
    //TODO: persistence id is a bit odd...fix
      readJournal.currentEventsByPersistenceId(s"customer-$customerId-customer-$customerId", 0, Long.MaxValue)

    // materialize stream, consuming events
    val futureRes = source.runFold(List[String]())((l, eventEnv) => eventEnv.event.toString :: l)

    onSuccess(futureRes) { result =>
      complete(result)
    }
  }

  def updateOperation(customerId: String, uuid: String, update: UpdateRecord) = {
    val futureRes = shardRegion ? Update(s"customer-$customerId", uuid, update) map {
      case CommandResult.Ok => HttpResponse(StatusCodes.OK)
      case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
    }

    onSuccess(futureRes) { result =>
      complete(result)
    }
  }

  def deleteOperation(customerId: String, uuid: String) = {
    val futureRes = shardRegion ? Delete(s"customer-$customerId", uuid) map {
      case CommandResult.Ok => HttpResponse(StatusCodes.OK)
      case CommandResult.Rejected => HttpResponse(StatusCodes.NotAcceptable)
    }

    onSuccess(futureRes) { result =>
      complete(result)
    }
  }
}
