package com.monkeygroover.backend

import spray.json._
import DefaultJsonProtocol._

// domain model
case class Record(name: String, data1: String, data2: Option[String] = None)

case object Record {
  implicit val Marshaller: RootJsonFormat[Record] = jsonFormat3(Record.apply)
}

object CustomerState {

  def empty: CustomerState = CustomerState(records = List.empty)

  trait CustomerDomainEvent
  case class RecordAccepted(record: Record) extends CustomerDomainEvent
}

case class CustomerState private (private val records: List[Record])
{
  import CustomerState._
  def getRecords() = records
  def recordCount() = records.size

  def updated(event: CustomerDomainEvent): CustomerState = event match {
    case RecordAccepted(record) => copy(record :: records)
  }
}