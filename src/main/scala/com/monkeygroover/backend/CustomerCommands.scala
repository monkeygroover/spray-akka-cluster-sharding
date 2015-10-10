package com.monkeygroover.backend

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class PartialRecord(name: String, data1: String, data2: String)
case object PartialRecord {
  implicit val Marshaller: RootJsonFormat[PartialRecord] = jsonFormat3(PartialRecord.apply)
}

case class UpdateRecord(name: Option[String] = None, data1: Option[String] = None, data2: Option[String] = None)
case object UpdateRecord {
  implicit val Marshaller: RootJsonFormat[UpdateRecord] = jsonFormat3(UpdateRecord.apply)
}


sealed trait CustomerCommands
case class Add(customerId: String, record: PartialRecord) extends CustomerCommands
case class Get(customerId: String) extends CustomerCommands
case class Delete(customerId: String, uuid: String) extends CustomerCommands
case class Update(customerId: String, uuid: String, record: UpdateRecord) extends CustomerCommands