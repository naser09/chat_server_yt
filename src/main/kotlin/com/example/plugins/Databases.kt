package com.example.plugins

import com.example.user.User
import com.example.user.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*

fun Application.configureDatabases(userRepository: UserRepository) {

    routing {
        // Create user
        post("/users") {
            val user = call.receive<User>()
            val id = userRepository.create(user)
            call.respond(HttpStatusCode.Created, id)
        }
            // Read user
        get("/users/{id}") {
            val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
            val user = userRepository.read(id)
            if (user != null) {
                call.respond(HttpStatusCode.OK, user)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        
            // Update user
        put("/users/{id}") {
            val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
            val user = call.receive<User>()
            userRepository.update(user)
            call.respond(HttpStatusCode.OK)
        }
        
            // Delete user
        delete("/users/{id}") {
            val id = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Invalid ID")
            userRepository.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
