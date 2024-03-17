package com.fastcampus.userservice.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.fastcampus.userservice.config.JWTProperties
import java.util.*

object JWTUtils {

    fun createToken(claim: JWTClaim, properties: JWTProperties) =
        JWT.create()
            .withIssuer(properties.issuer)
            .withSubject(properties.subject)
            .withIssuedAt(Date())
            .withExpiresAt(Date(Date().time + properties.expiresTime * 1000))
            .withClaim("userId", claim.userId)
            .withClaim("email", claim.email)
            .withClaim("profileUrl", claim.profileUrl)
            .withClaim("username", claim.username)
            .sign(Algorithm.HMAC256(properties.secret))

    fun decode(token: String, secret: String, issuer: String): DecodedJWT {
        val algorithm = Algorithm.HMAC256(secret) // 요청 받은 secret 키로 알고리즘을 생성한다. 토큰을 서명하고 검증하는 데 쓰인다.

        val verifier = JWT.require(algorithm) // jwt 검증자를 설정. 검증에 사용할 알고리즘 설정
            .withIssuer(issuer) // jwt 발급자를 설정
            .build() // 빌드

        return verifier.verify(token) // verifier (검증자)로 해당 token 을 verify 검증한다.
        // verify() 메서드는 토큰이 유효하면 해당 토큰을 해독한 DecodedJWT 객체를 반환한다.
    }

}


data class JWTClaim(
    val userId: Long,
    val email: String,
    val profileUrl: String,
    val username: String,
)