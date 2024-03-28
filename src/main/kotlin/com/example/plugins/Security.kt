package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.user.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureSecurity(userRepository: UserRepository) {
    authentication {
        basic(name = "basic") {
            realm = "Ktor Server"
            validate { credentials ->
                val user = userRepository.read(credentials.name)
                if (user?.password == credentials.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    
        form(name = "form") {
            userParamName = "user"
            passwordParamName = "password"
            validate { credentials ->
                if (credentials.name == "naser" && credentials.password == "password") {
                    UserIdPrincipal(credentials.name)
                } else null
            }
            challenge {
                call.respond(HttpStatusCode.Unauthorized ,"Failed At Form Auth")
            }
        }
    }
    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    authentication {
        jwt {
            realm = jwtRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(jwtSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized,"jwt auth failed")
            }
        }
    }
    routing {
        authenticate("basic") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        authenticate("form") {
            get("/protected/route/form") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }
        authenticate {
            get("/auth"){
                call.respond(HttpStatusCode.OK,"authenticated")
            }
        }
    }
}