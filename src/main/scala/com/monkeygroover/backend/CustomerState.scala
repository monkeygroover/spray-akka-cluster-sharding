package com.monkeygroover.backend

import spray.json._
import DefaultJsonProtocol._

// domain model
case class Record(uuid: String, name: String, data1: String, data2: Option[String] = None)

case object Record {
  implicit val Marshaller: RootJsonFormat[Record] = jsonFormat4(Record.apply)
}

object CustomerState {

  def empty: CustomerState = CustomerState(recordMap = Map.empty)

  trait CustomerDomainEvent
  case class RecordAccepted(record: Record) extends CustomerDomainEvent
  case class RecordDeleted(uuid: String) extends CustomerDomainEvent
}

case class CustomerState private (private val recordMap: Map[String, Record])
{
  import CustomerState._
  def recordList() = recordMap.values.toList

  def acceptAddition() = recordMap.size != 10
  def checkExists(uuid: String) = recordMap.contains(uuid)
  def updated(event: CustomerDomainEvent): CustomerState = event match {
    case RecordAccepted(record) => copy(recordMap.updated(record.uuid, record))
    case RecordDeleted(uuid) => copy(recordMap - uuid)
  }
}