package com.fastcampus.issueservice.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller //Json 이 아닌 html 페이지를 보여줌
class PageController {

    @GetMapping(value = ["", "/index"])
    fun index() = "index"

    @GetMapping("/issueapp")
    fun issueApp() = "issueapp"

    @GetMapping("/signup")
    fun signUp() = "signup"

}