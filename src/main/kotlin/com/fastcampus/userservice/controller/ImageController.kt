package com.fastcampus.userservice.controller

import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.File
import java.io.FileInputStream

@Controller // json 이 아닌 이미지 경로를 응답하는 컨트롤러이기 때문에 RestController 가 아닌 일반 Controller
@RequestMapping("/images")
class ImageController {

    @GetMapping("{filename}")
    fun image(@PathVariable filename: String): ResponseEntity<InputStreamResource> {
        val ext = filename.substring(filename.lastIndexOf("." +1))
        val file = File(ClassPathResource("/images/").file, filename)

        // images/jpg, images/png, 업로드 확장자에 따라 헤더에 콘텐트 타입으로 응답
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_TYPE, "images/$ext")
            .body(InputStreamResource(FileInputStream(file)))
    }


}