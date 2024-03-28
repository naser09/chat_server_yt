package com.example.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.sql.Database
import java.sql.SQLIntegrityConstraintViolationException

fun Application.userRoute(userRepository: UserRepository){
    val tokenRepository = TokenRepository()
    routing {
        post("/registration"){
            val user = call.receive<User>()
            try {
                val id = userRepository.create(user)
                call.respond(HttpStatusCode.Created,id)
            }catch (ex:JdbcSQLIntegrityConstraintViolationException){
                call.respond(HttpStatusCode.NotAcceptable,"username exist !")
            }catch (ex:Exception){
                call.respond(HttpStatusCode.ExpectationFailed)
            }
        }
        authenticate("basic"){
            get("login"){
                val username = call.principal<UserIdPrincipal>()!!.name
                val token = tokenRepository.generateToken(username)
                call.respond(HttpStatusCode.OK,token)
            }
        }
    }
}