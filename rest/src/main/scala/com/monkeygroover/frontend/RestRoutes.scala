package com.monkeygroover.frontend

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.model.StatusCodes
import akka.stream.Materializer
import com.monkeygroover.commands._

/**
 * Created by monkeygroover on 09/10/15.
 */
abstract class RestRoutes(implicit system: ActorSystem, materializer: Materializer) extends Directives {

  lazy val routes = ping ~ addData ~ updateData ~ deleteData ~ getData ~ getHistory

  val ping = path("ping") & complete(StatusCodes.OK)

  val addData =
    post {
      path("customer" / Segment) { customerId =>
        entity(as[PartialRecord]) { record =>
          addOperation(customerId, record)
        }
      }
    }

  def addOperation(customerId: String, record: PartialRecord): Route

  val getData =
      get {
        path("customer" / Segment) { customerId =>
          getOperation(customerId)
        }
      }

  def getOperation(customerId: String): Route

  val getHistory =
    get {
      path("customer" / Segment / "history") { customerId =>
        getHistoryOperation(customerId)
      }
    }

  def getHistoryOperation(customerId: String): Route

  val updateData =
      patch {
        path("customer" / Segment / Segment) { (customerId, uuid) =>
          entity(as[UpdateRecord]) { update =>
            updateOperation(customerId, uuid, update)
          }
        }
      }

  def updateOperation(customerId: String, uuid: String, update: UpdateRecord): Route

  val deleteData =
      post {
        path("customer" / Segment / Segment) { (customerId, uuid) =>
          deleteOperation(customerId, uuid)
        }
      }

  def deleteOperation(customerId: String, uuid: String): Route
}
