package com.example.blog

import com.example.user.CustomListSerializer
import com.example.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class BlogRepository(private val database: Database) {
    object BlogTable:Table(){
        val id = integer("id").autoIncrement()
        val post  = largeText("post")
        val author = long("author").references(UserRepository.Users.id, onDelete = ReferenceOption.CASCADE)
        val likes = largeText("likes")

        override val primaryKey: PrimaryKey
            get() = PrimaryKey(id)
    }
    init {
        transaction(database){ SchemaUtils.create(BlogTable) }
    }
    suspend fun <T> dbQuery(block:suspend ()->T):T = newSuspendedTransaction(Dispatchers.IO) { block() }

    suspend fun create(blog: Blog):Int = dbQuery {
        BlogTable.insert {
            it[post] = blog.post
            it[author] = blog.author
            it[likes] = Json.encodeToString(CustomListSerializer,blog.likes)
        }[BlogTable.id]
    }
    suspend fun getAllBlog():List<Blog> = dbQuery {
        BlogTable.selectAll().map {
            Blog(
                it[BlogTable.id],
                it[BlogTable.post],
                it[BlogTable.author],
                Json.decodeFromString(it[BlogTable.likes])
            )
        }
    }
    suspend fun likesBlog(userId:Long,blogId:Int):Boolean = dbQuery {
        val likeString = BlogTable.select(where =  BlogTable.id eq blogId).first()[BlogTable.likes]
        val like = Json.decodeFromString(CustomListSerializer,likeString)
        if (!like.contains(userId)){
            BlogTable.update ( where = {BlogTable.id.eq(blogId) }){
                it[likes] = Json.encodeToString(CustomListSerializer,like+listOf(userId))
            }
            true
        }else false
    }
}