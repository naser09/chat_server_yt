package com.example

import com.example.plugins.*
import com.example.user.UserRepository
import com.example.user.userRoute
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSockets()
    configureSerialization()

    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = ""
    )
    val userRepository = UserRepository(database)
    configureSecurity(userRepository)
    configureDatabases(userRepository)
    configureRouting()
    //new code
    userRoute(userRepository)
}
