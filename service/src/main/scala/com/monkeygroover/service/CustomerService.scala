package com.monkeygroover.service

import java.util.UUID

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ActorLogging, Props, ReceiveTimeout}
import akka.cluster.sharding.ShardRegion
import akka.cluster.sharding.ShardRegion.Passivate
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.monkeygroover.commands._
import com.monkeygroover.persistence.{CustomerState, Record, RecordPartialUpdate}
import shapeless._
import shapeless.syntax.singleton._

import scala.concurrent.duration._

object CustomerService {
  
  def props: Props = Props(new CustomerService())
  
  object Shard {
    val name = "CustomerService"

    // sharding on customer id
    val entityIdExtractor: ShardRegion.ExtractEntityId = {
      case addMsg @ Add(customerId, _)  => (customerId , addMsg)
      case getMsg @ Get(customerId) => (customerId , getMsg)
      case deleteMsg @ Delete(customerId, _) => (customerId , deleteMsg)
      case updateMsg @ Update(customerId, _, _) => (customerId, updateMsg)
    }

    // sharding on customer id
    val shardIdExtractor: ShardRegion.ExtractShardId = {
      case Add(customerId, _) => customerId
      case Get(customerId) => customerId
      case Delete(customerId, _) => customerId
      case Update(customerId, _, _) => customerId
    }
  }
}

class CustomerService extends PersistentActor with ActorLogging {
  log.debug(s"instantiating CustomerService for shard: ${self.path.name}")

  override def persistenceId: String = self.path.name

  // unload from memory if no messages received in the following time-period
  context.setReceiveTimeout(5.minutes)

  private var customerState = CustomerState.empty

  import CustomerState._
  override def receiveRecover: Receive = {
    case event: CustomerDomainEvent =>
      // only update current state by applying the event, no side effects
      customerState = customerState.updated(event)
      log.debug(s"Replayed ${event.getClass.getSimpleName}")
    case RecoveryCompleted => log.debug("recovery completed for customer")
  }

  override def receiveCommand: Receive = {
    case Add(customerId, partialRecord) =>
      if (customerState.count != 10) {
        // create a Record from the PartialRecord adding a uuid
        val partialRec = LabelledGeneric[PartialRecord].to(partialRecord)
        val uuidRec = 'uuid ->> UUID.randomUUID.toString
        val record = LabelledGeneric[Record].from(uuidRec :: partialRec)
        log.debug(s"submitting new record $record for $customerId")
        persist(RecordAccepted(record)) { event =>
          customerState = customerState.updated(event)
          sender ! CommandResult.Ok
        }
      } else {
        sender ! CommandResult.Rejected
      }
    case Update(customerId, uuid, updateRecord) =>
      if (customerState.checkExists(uuid)) {
        val inRec = LabelledGeneric[UpdateRecord].to(updateRecord)
        val recordPartialUpdate = LabelledGeneric[RecordPartialUpdate].from(inRec)

        log.debug(s"updating record $uuid with $updateRecord for $customerId")
        persist(RecordUpdated(uuid, recordPartialUpdate)) { event =>
          customerState = customerState.updated(event)
          sender ! CommandResult.Ok
        }
      } else {
        sender ! CommandResult.Rejected
      }
    case Delete(customerId, uuid) =>
      if (customerState.checkExists(uuid)) {
        log.debug(s"deleting record $uuid for $customerId")
        persist(RecordDeleted(uuid)) { event =>
          customerState = customerState.updated(event)
          sender ! CommandResult.Ok
        }
      } else {
        sender ! CommandResult.Rejected
      }
    case Get(_) => sender ! customerState.recordList

    case ReceiveTimeout => {
      log.debug(s"unloading ${self.path.name}")
      context.parent ! Passivate(stopMessage = Stop)
    }
    case Stop => context.stop(self)
  }
}