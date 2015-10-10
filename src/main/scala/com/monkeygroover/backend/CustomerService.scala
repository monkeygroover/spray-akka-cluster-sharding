package com.monkeygroover.backend

import akka.actor.{ActorLogging, Props}
import akka.cluster.sharding.ShardRegion
import akka.persistence.{PersistentActor, RecoveryCompleted}
import java.util.UUID
import shapeless._
import syntax.singleton._

object CustomerService {
  
  def props: Props = Props(new CustomerService())
  
  object Shard {
    val name = "CustomerService"

    // sharding on customer id
    val entityIdExtractor: ShardRegion.ExtractEntityId = {
      case arMsg @ AddRecord(customerId, _)  => (customerId , arMsg)
      case getRecsMsg @ GetRecords(customerId) => (customerId , getRecsMsg)
      case delRecMsg @ DeleteRecord(customerId, _) => (customerId , delRecMsg)
    }

    // sharding on customer id
    val shardIdExtractor: ShardRegion.ExtractShardId = {
      case AddRecord(customerId, _) => customerId
      case GetRecords(customerId)         => customerId
      case DeleteRecord(customerId, _) => customerId
    }
  }
}

class CustomerService extends PersistentActor with ActorLogging {
  println("instantiating CustomerService for shard: {}", self.path.name)
  override def persistenceId: String = self.path.parent.name + "-" + self.path.name

  private var customerState = CustomerState.empty

  import CustomerState._
  override def receiveRecover: Receive = {
    case event: CustomerDomainEvent =>
      // only update current state by applying the event, no side effects
      customerState = customerState.updated(event)
      println("Replayed {}", event.getClass.getSimpleName)
    case RecoveryCompleted => println("recovery completed for customer")
  }

  override def receiveCommand: Receive = {
    case AddRecord(custId, partialRecord) =>
      if (customerState.acceptAddition) {
        // create a Record from the PartialRecord adding a uuid
        val partialRec = LabelledGeneric[PartialRecord].to(partialRecord)
        val uuidRec = 'uuid ->> UUID.randomUUID.toString
        val record = LabelledGeneric[Record].from(uuidRec :: partialRec)
        println(s"submitting new record $record for $custId")
        persist(RecordAccepted(record)) { event =>
          customerState = customerState.updated(event)
          sender ! CommandResult.Ok
        }
      } else {
        sender ! CommandResult.Rejected
      }
    case DeleteRecord(custId, uuid) =>
      if (customerState.checkExists(uuid)) {
        // create a Record from the PartialRecord adding a uuid
        println(s"deleting record $uuid for $custId")
        persist(RecordDeleted(uuid)) { event =>
          customerState = customerState.updated(event)
          sender ! CommandResult.Ok
        }
      } else {
        sender ! CommandResult.Rejected
      }
    case GetRecords(_) => sender ! customerState.recordList
  }
}