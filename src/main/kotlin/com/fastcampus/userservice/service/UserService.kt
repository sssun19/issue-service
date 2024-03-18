package com.fastcampus.userservice.service

import com.auth0.jwt.interfaces.DecodedJWT
import com.fastcampus.userservice.config.JWTProperties
import com.fastcampus.userservice.domain.entity.User
import com.fastcampus.userservice.domain.repository.UserRepository
import com.fastcampus.userservice.exception.UserExistsException
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
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserService(
    private val userRepository: UserRepository,
) {

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

}