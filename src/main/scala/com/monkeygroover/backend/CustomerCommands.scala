package com.monkeygroover.backend

sealed trait CustomerCommands
case class AddRecord(customerId: String, record: Record) extends CustomerCommands
case class GetRecords(customerId: String) extends CustomerCommands