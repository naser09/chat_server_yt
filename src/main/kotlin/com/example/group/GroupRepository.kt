package com.example.group

import com.example.user.CustomListSerializer
import com.example.user.UserRepository
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class GroupRepository(private val database: Database){

    object GroupTable : Table() {
        val id = long("group_id").autoIncrement().uniqueIndex()
        val name = text("group_name")
        val owner = long("owner").references(UserRepository.Users.id, onDelete = ReferenceOption.CASCADE)
        val members = largeText("members")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(GroupTable)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(group: Group): Long = dbQuery {
        GroupTable.insert {
            it[name] = group.name
            it[owner] = group.owner
            it[members] = Json.encodeToString(CustomListSerializer,group.members)
        }[GroupTable.id]
    }

    suspend fun readByGroupId(groupId:Long): Group? = dbQuery {
        GroupTable.select(where = { GroupTable.id.eq(groupId) }).map {
            Group(
                id = it[GroupTable.id],
                name = it[GroupTable.name],
                owner = it[GroupTable.owner],
                members = Json.decodeFromString(CustomListSerializer,it[GroupTable.members])
            )
        }.firstOrNull()
    }

    suspend fun readByGroupId(groupList:List<Long>): List<Group> = dbQuery {
        GroupTable.selectAll().andWhere { GroupTable.id.inList(groupList) }.map {
            Group(
                id = it[GroupTable.id],
                name = it[GroupTable.name],
                owner = it[GroupTable.owner],
                members = Json.decodeFromString(CustomListSerializer,it[GroupTable.members])
            )
        }
    }
suspend fun readByUserId(userId:Long):List<Group> = dbQuery {
    GroupTable.selectAll().andWhere {
        GroupTable.members.like("[$userId,") or
                GroupTable.members.like(",$userId,") or
                    GroupTable.members.like(",$userId]")
    }.map {
        Group(
            id = it[GroupTable.id],
            name = it[GroupTable.name],
            owner = it[GroupTable.owner],
            members = Json.decodeFromString(CustomListSerializer,it[GroupTable.members])
        )
    }
}
    suspend fun update(group: Group) {
        dbQuery {
            GroupTable.update({ GroupTable.id eq group.id}) {
                it[name] = group.name
                it[owner] = group.owner
                it[members] = Json.encodeToString(CustomListSerializer,group.members)
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            GroupTable.deleteWhere { GroupTable.id.eq(id) }
        }
    }
}