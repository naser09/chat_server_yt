package com.example.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

class TokenRepository {
    val jwtAudience = "jwt-audience"
    val jwtDomain = "https://jwt-provider-domain/"
    val jwtRealm = "ktor sample app"
    val jwtSecret = "secret"
    fun generateToken(username:String) = JWT
        .create()
        .withAudience(jwtAudience)
        .withIssuer(jwtDomain)
        .withClaim("username",username)
        .sign(Algorithm.HMAC256(jwtSecret))
}