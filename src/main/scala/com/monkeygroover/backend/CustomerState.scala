package com.monkeygroover.backend

import spray.json._
import DefaultJsonProtocol._

// domain model
case class Record(uuid: String, name: String, data1: String, data2: String)

case object Record {
  implicit val Marshaller= jsonFormat4(Record.apply)
}

case class RecordPartialUpdate(name: Option[String] = None, data1: Option[String] = None, data2: Option[String] = None)

case object RecordPartialUpdate {
  implicit val Marshaller = jsonFormat3(RecordPartialUpdate.apply)
}


object CustomerState {

  def empty: CustomerState = CustomerState(recordMap = Map.empty)

  trait CustomerDomainEvent
  case class RecordAccepted(record: Record) extends CustomerDomainEvent
  case class RecordUpdated(uuid: String, partialUpdate: RecordPartialUpdate) extends CustomerDomainEvent
  case class RecordDeleted(uuid: String) extends CustomerDomainEvent
}

case class CustomerState private (private val recordMap: Map[String, Record])
{
  import CustomerState._
  def recordList() = recordMap.values.toList

  def count() = recordMap.size
  def checkExists(uuid: String) = recordMap.contains(uuid)
  def updated(event: CustomerDomainEvent): CustomerState = event match {
    case RecordAccepted(record) => copy(recordMap.updated(record.uuid, record))
    case RecordUpdated(uuid, partialUpdate) => {
      recordMap.get(uuid).fold(this){ oldRecord =>
        //replace all fields in old record where partial update is Some(x)
        val newRecord = oldRecord.copy(
          name = partialUpdate.name.getOrElse(oldRecord.name),
          data1 = partialUpdate.data1.getOrElse(oldRecord.data1),
          data2 = partialUpdate.data2.getOrElse(oldRecord.data2)
        )

        copy(recordMap.updated(uuid, newRecord))
      }
    }
    case RecordDeleted(uuid) => copy(recordMap - uuid)
  }
}