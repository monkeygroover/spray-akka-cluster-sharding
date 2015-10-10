package com.monkeygroover.backend

import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class PartialRecord(name: String, data1: String, data2: Option[String] = None)
case object PartialRecord {
  implicit val Marshaller: RootJsonFormat[PartialRecord] = jsonFormat3(PartialRecord.apply)
}

sealed trait CustomerCommands
case class AddRecord(customerId: String, record: PartialRecord) extends CustomerCommands
case class GetRecords(customerId: String) extends CustomerCommands
case class DeleteRecord(customerId: String, uuid: String) extends CustomerCommands