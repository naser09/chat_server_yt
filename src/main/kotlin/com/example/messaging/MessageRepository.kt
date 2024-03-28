package com.example.messaging

import com.example.user.UserRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class MessageRepository(private val database: Database){

    object MessageTable : Table() {
        val id = uuid("id").autoGenerate().uniqueIndex()
        val message = text("message")
        val sender = long("sender").references(UserRepository.Users.id, onDelete = ReferenceOption.NO_ACTION)
        val time = long("time")
        val receiver = long("receiver").references(UserRepository.Users.id, onDelete = ReferenceOption.CASCADE)
        val isGroup = bool("isGroup").default(defaultValue = false)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(MessageTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(message: Message): UUID = dbQuery {
        MessageTable.insert {
            it[MessageTable.message] = message.message
            it[sender] = message.sender
            it[receiver] = message.receiver
            it[time] = message.time
            it[isGroup] = message.isGroup
        }[MessageTable.id]
    }

    suspend fun readBySender(id: Long): List<Message> {
        return dbQuery {
            MessageTable.select {
                MessageTable.sender.eq(id)
            }.map {
                Message(
                    it[MessageTable.id].toString(),
                    it[MessageTable.message],
                    it[MessageTable.sender],
                    it[MessageTable.time],
                    it[MessageTable.receiver],
                    it[MessageTable.isGroup]
                )
            }
        }
    }

    suspend fun readByReceiver(id: Long): List<Message> {
        return dbQuery {
            MessageTable.select {
                MessageTable.receiver.eq(id)
            }.map {
                Message(
                    it[MessageTable.id].toString(),
                    it[MessageTable.message],
                    it[MessageTable.sender],
                    it[MessageTable.time],
                    it[MessageTable.receiver],
                    it[MessageTable.isGroup]
                )
            }
        }
    }
    suspend fun getAllMessage():List<Message> = dbQuery{
        MessageTable.selectAll().map {
            Message(
                it[MessageTable.id].toString(),
                it[MessageTable.message],
                it[MessageTable.sender],
                it[MessageTable.time],
                it[MessageTable.receiver],
                it[MessageTable.isGroup]
            )
        }
    }
    suspend fun read(uuid:String): Message? {
        return dbQuery {
            MessageTable.select { MessageTable.id.eq(UUID.fromString(uuid)) }
                .map {
                    Message(
                        it[MessageTable.id].toString(),
                        it[MessageTable.message],
                        it[MessageTable.sender],
                        it[MessageTable.time],
                        it[MessageTable.receiver],
                        it[MessageTable.isGroup]
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(message: Message) {
        dbQuery {
            MessageTable.update({ MessageTable.id eq UUID.fromString(message.id) }) {

                it[MessageTable.message] = message.message
                it[sender] = message.sender
                it[receiver] = message.receiver
                it[time] = message.time
                it[isGroup] = message.isGroup
            }
        }
    }

    suspend fun delete(uuid: String) {
        dbQuery {
            MessageTable.deleteWhere { MessageTable.id.eq(UUID.fromString(uuid)) }
        }
    }
}