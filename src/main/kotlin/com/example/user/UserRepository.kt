package com.example.user

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) {
    object Users : Table() {
        val id = long("id").autoIncrement()
        val username = varchar("username", length = 50).uniqueIndex()
        val password = varchar("password", length = 50)
        val inbox = largeText("inbox")
        val friends = largeText("friends")

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(user: User): Long = dbQuery {
        Users.insert {
            it[username] = user.username
            it[password] = user.password
            it[inbox] = Json.encodeToString(CustomListSerializer,user.inbox)
            it[friends] =  Json.encodeToString(CustomListSerializer,user.friends)
        }[Users.id]
    }

    suspend fun read(id: Long): User? {
        return dbQuery {
            Users.select { Users.id eq id }
                .map {
                    User(
                        it[Users.id],
                        it[Users.username],
                        it[Users.password],
                        Json.decodeFromString(CustomListSerializer,it[Users.inbox]),
                        Json.decodeFromString(CustomListSerializer,it[Users.friends])

                    )
                }
                .singleOrNull()
        }
    }
    suspend fun getAllUser():List<User> = dbQuery{
        Users.selectAll().map {
            User(
                it[Users.id],
                it[Users.username],
                it[Users.password],
                Json.decodeFromString(CustomListSerializer,it[Users.inbox]),
                Json.decodeFromString(CustomListSerializer,it[Users.friends])
            ) }
    }
    suspend fun addFriends(friendsId:Long,userId:Long){
        val friends = Users.select(where = {Users.id.eq(userId)})
            .map { Json.decodeFromString(CustomListSerializer,it[Users.friends]) }
            .firstOrNull()
        if (friends !=null && !friends.contains(friendsId)){
            val  newList = friends.toMutableSet()
            newList.add(friendsId)
            Users.update(where = {Users.id eq userId}){
                it[Users.friends] = Json.encodeToString(CustomListSerializer,newList.toList())
            }
        }
    }
    suspend fun read(username:String): User? {
        return dbQuery {
            Users.select { Users.username.eq(username) }
                .map {
                    User(
                        it[Users.id],
                        it[Users.username],
                        it[Users.password],
                        Json.decodeFromString(CustomListSerializer,it[Users.inbox]),
                        Json.decodeFromString(CustomListSerializer,it[Users.friends]),
                    )
                }
                .singleOrNull()
        }
    }

    suspend fun update(user: User) {
        dbQuery {
            Users.update({ Users.id eq user.id }) {
                it[username] = user.username
                it[password] = user.password
                it[inbox] = Json.encodeToString(CustomListSerializer,user.inbox)
                it[friends] =  Json.encodeToString(CustomListSerializer,user.friends)
            }
        }
    }

    suspend fun delete(id: Long) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }
}