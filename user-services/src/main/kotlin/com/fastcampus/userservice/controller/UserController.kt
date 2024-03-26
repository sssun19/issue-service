package com.fastcampus.userservice.controller

import com.fastcampus.userservice.model.*
import com.fastcampus.userservice.service.UserService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.*
import reactor.kotlin.core.publisher.toMono
import com.fastcampus.userservice.model.*
import java.io.File

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/signup")
    suspend fun signUp(@RequestBody request: SignUpRequest) {
        userService.signUp(request)
    }

    @PostMapping("/signin")
    suspend fun signIn(@RequestBody singInRequest: SignInRequest) : SignInResponse {
        return userService.signIn(singInRequest)
    }

    @DeleteMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun logout(@AuthToken token: String) { //@AuthToken annotation 이 있는 경우 HandlerMethodArgumentResolver 에서 token 객체에 넣어주는 기능 구현.
        return userService.logout(token)
    }

    @GetMapping("/me") // ${userId} path variable 로 아이디를 받지 않는 이유는 해커가 아이디를 유추하기만 하면 상세조회 페이지로 바로 넘어갈 수 있는 위험성 때문에.
    suspend fun get(
        @AuthToken token: String,
    ) : MeResponse {
        return MeResponse(userService.getByToken(token))
    }

    @GetMapping("/{userId}/username")
    suspend fun getUsername(@PathVariable userId: Long) : Map<String, String> {
        return mapOf("reporter" to userService.get(userId).username)
    }

    @PostMapping("/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]) // 이미지를 포함하여 서버에 요청
    suspend fun edit(
        @PathVariable id: Long,
        @ModelAttribute request: UserEditRequest, // content type 이 multipart form data 라서 modelAttribute
        @AuthToken token: String,
        @RequestPart("profileUrl") filePart: FilePart,
    ) {

        val orgFilename = filePart.filename() // 파일 업로드를 시도한 실제 파일명
        var filename: String? = null

        if (orgFilename.isNotEmpty()) {
            // abc.jpg < 콤마 뒤의 +1 인덱스 위치 멤버부터 가져오는 방법 (확장자명)
            val ext = orgFilename.substring(orgFilename.lastIndexOf(".") +1)
            filename = "${id}.${ext}" // 1.jpg, 3.png,...

            // /resources/images/1.jpg 을 ClassPathResource 메서드로 탐색
            val file = File(ClassPathResource("/images/").file, filename)
            filePart.transferTo(file).awaitSingleOrNull()

        }

        userService.edit(token, request.username, filename)

    }

}