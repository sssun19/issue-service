package com.fastcampus.userservice.service

import com.auth0.jwt.interfaces.DecodedJWT
import com.fastcampus.userservice.config.JWTProperties
import com.fastcampus.userservice.domain.entity.User
import com.fastcampus.userservice.domain.repository.UserRepository
import com.fastcampus.userservice.exception.PasswordNotMatchedException
import com.fastcampus.userservice.exception.UserExistsException
import com.fastcampus.userservice.exception.UserNotFoundException
import com.fastcampus.userservice.model.SignInRequest
import com.fastcampus.userservice.model.SignInResponse
//import com.fastcampus.userservice.exception.InvalidJwtTokenExceptionException
//import com.fastcampus.userservice.exception.PasswordNotMatchedException
//import com.fastcampus.userservice.exception.UserExistsException
//import com.fastcampus.userservice.exception.UserNotFoundException
//import com.fastcampus.userservice.model.SignInRequest
//import com.fastcampus.userservice.model.SignInResponse
import com.fastcampus.userservice.model.SignUpRequest
import com.fastcampus.userservice.utils.BCryptUtils
import com.fastcampus.userservice.utils.JWTClaim
import com.fastcampus.userservice.utils.JWTUtils
import jdk.jshell.spi.ExecutionControl.UserException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.Duration

@Service
class UserService(
    private val userRepository: UserRepository,
    private val jwtProperties: JWTProperties,
    private val cacheManager: CoroutineCacheManager<User>,
) {

    companion object {
        private val CACHE_TTL = Duration.ofMinutes(1)
    }

    suspend fun signUp(signUpRequest: SignUpRequest) {
        with(signUpRequest) {
            userRepository.findByEmail(email)?.let {
                throw UserExistsException()
            }

            val user = User(
                email = email,
                password = BCryptUtils.hash(password), // 요청 들어온 비밀번호 그대로 받으면 보안 취약점 발생. BCrypt로 암호화 필수.
                username = username
            )
            userRepository.save(user)
        }
    }

    suspend fun signIn(signInRequest: SignInRequest): SignInResponse {
        return with(userRepository.findByEmail(signInRequest.email) ?: throw UserNotFoundException()) {
            val verified = BCryptUtils.verify(signInRequest.password, password)

            if(!verified) {
                throw PasswordNotMatchedException()
            }

            val jwtClaim = JWTClaim(
                userId = id!!,
                email = email,
                profileUrl = profileUrl,
                username = username
            )

            val token = JWTUtils.createToken(jwtClaim, jwtProperties)

            cacheManager.awaitPut(key = token, value = this, ttl = CACHE_TTL)

            SignInResponse(
                email = email,
                username = username,
                token = token,
            )
        }

    }

    suspend fun logout(token: String) {

        cacheManager.awaitEvict(token)

    }

    fun getByToken(token: String): User {

        val decodedJWT = JWTUtils.decode(token, jwtProperties.secret, jwtProperties.issuer)

        cacheManager.awaitGetOrPut(key = token, ttl = CACHE_TTL)
    }

}