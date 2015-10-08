package com.monkeygroover.backend

// domain model
case class Record(data1: String, data2: String)

object CustomerState {

  def empty: CustomerState = CustomerState(records = List.empty)

  trait CustomerDomainEvent
  case class RecordAccepted(record: Record) extends CustomerDomainEvent
}

case class CustomerState private (private val records: List[Record])
{
  import CustomerState._
  
  def recordCount() = records.size

  def updated(event: CustomerDomainEvent): CustomerState = event match {
    case RecordAccepted(record) => copy(record :: records)
  }
}