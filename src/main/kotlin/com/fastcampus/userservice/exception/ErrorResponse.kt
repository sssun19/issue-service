package com.fastcampus.userservice.exception

data class ErrorResponse( // Http Status 만으로는 모든 에러를 처리하기 불편. client 와 server 가 약속한 에러 코드를 내려줄 클래스
    val code: Int,
    val message: String, // Exception 의 메세지 내용 stacktrace 와 같은 주요 정보가 노출되면 보안이 취약. 보안 사고 발생 가능성 높음.
    // client 에 그대로 응답을 내려주면 안 됨.
)