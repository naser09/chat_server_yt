package com.example.group

import com.example.user.CustomListSerializer
import com.example.user.User
import com.example.user.UserRepository
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import kotlin.concurrent.thread

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
//Test class
fun main() = runBlocking{
    val list = listOf<Long>(10,1,2,3,5,45,89,65,123,789,56)
    val string = Json.encodeToString(CustomListSerializer,list)
    println(string)
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )
    val userRepository = UserRepository(database)
    val repo = GroupRepository(database)
    val groups = (1..10).map { Group(it.toLong(),"group no :$it",it.toLong(), listOf(it.toLong())) }
    val owners = groups.map { User(0,"owner ${it.id}","pass") }
    val scope = GlobalScope.launch {
        owners.forEach { userRepository.create(it) }
        groups.forEach { repo.create(it) }
        val data = repo.readByGroupId(groups.map { it.id })
        data.forEach {
            println(it)
        }
    }
    scope.join()
}