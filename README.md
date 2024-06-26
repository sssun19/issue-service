﻿# issue-service

### issue-service / user-service 멀티 모듈로 이슈 관리 api 개발

🧑‍🏫 유저 정보를 JWT 토큰으로 관리하기

- 의존성 추가

    ![image](https://github.com/sssun19/issue-service/assets/125242481/9f43936a-454c-4c69-b9d2-51aca6f631eb)

- JWTClaim 과 JWTProperties<br/>

    👀 JWT (Json Web Token) 는 클레임 claim 과 서명 signature 객체를 가지고 있다. 클레임 객체는 유저의 아이디(식별자), 이메일, 프로필사진, 유저 이름 정보를 가지고 있다. <br/>
    
    ![image](https://github.com/sssun19/issue-service/assets/125242481/110f3c31-7afa-4ca0-8f48-8d2cbd0b668d)
    
    👀 JWTProperties 객체는 issuer (토큰 발급자), subject, expiresTime (토큰 만료 시간), secret (키) 정보를 포함한다. <br/>
    
    ![image](https://github.com/sssun19/issue-service/assets/125242481/4ee7409f-5ad0-400c-88de-9c5d52ae2df4)
    
    > **ConstructorBinding** : 해당 클래스의 프로퍼티를 final 로 선언하며, 값은 생성자를 통해 주입된다. 객체가 생성된 후에는 값을 변경할 수 없어 객체의 불변성을 보장. <br/>
    
    > **ConfigurationProperties** : 외부 설정 파일(application.yml)에서 prefix = "jwt" 로 지정한 부분을 읽어와 매핑.
    
    ![image](https://github.com/sssun19/issue-service/assets/125242481/92cdf724-dac9-4388-916a-7b6a0ffc1c56)

- JWTUtils<br/>

    ```
    fun createToken(claim: JWTClaim, properties: JWTProperties) =
        JWT.create()
            .withIssuer(properties.issuer)
            .withSubject(properties.subject)
            .withIssuedAt(Date())
            .withExpiresAt(Date(Date().time + properties.expiresTime * 1000)) // 토큰 만료 시간을 현재 시간에서 expiresTime(ms*1000) 초 후로 설정.
            .withClaim("userId", claim.userId)
            .withClaim("email", claim.email)
            .withClaim("profileUrl", claim.profileUrl)
            .withClaim("username", claim.username)
            .sign(Algorithm.HMAC256(properties.secret)) // 요청 받은 secret 키로 알고리즘을 생성하고 설정한다. 토큰을 서명하고 검증하는 데 쓰인다.
    ```
    createToken 메서드로 JWT 토큰 생성하고 검증한다.

    ```
    fun decode(token: String, secret: String, issuer: String): DecodedJWT {
        val algorithm = Algorithm.HMAC256(secret)

        val verifier = JWT.require(algorithm) // jwt 검증자를 설정. 검증에 사용할 알고리즘 설정
            .withIssuer(issuer) // jwt 발급자를 설정
            .build() // 빌드

        return verifier.verify(token) // verifier (검증자)로 해당 token 을 verify 검증한다.
        // verify() 메서드는 토큰이 유효하면 해당 토큰을 해독한 DecodedJWT 객체를 반환한다.
    }
    ```


#### ✨issue-service<br/>
* IssueController

    ```
    @PostMapping
    fun create(
        authUser: AuthUser,
        @RequestBody request: IssueRequest,
    ) = issueService.create(authUser.userId, request)
    ```

* IssueService

    ```
    @Transactional
    fun create(userId: Long, request: IssueRequest) : IssueResponse {
    
        val issue = Issue(
            summary = request.summary,
            description = request.description,
            userId = userId,
            type = request.type,
            priority = request.priority,
            status = request.status,
        )
        return IssueResponse(issueRepository.save(issue))
    }
    ```

    ![image](https://github.com/sssun19/issue-service/assets/125242481/a78cf4d3-4247-4665-bb53-6e491ffa90f2)
    
    > invoke 함수로 Issue 객체를 바로 전달. 댓글은 내림차순 정렬하여 map 으로 관리
    
    ![image](https://github.com/sssun19/issue-service/assets/125242481/c9378bc6-b529-4647-a3cc-02960e6f178b)

#### ✨user-service<br/>
* UserController

    ```
    @RestController
    @RequestMapping("/api/v1/users")
    class UserController(
        private val userService: UserService,
    ) {
        @PostMapping("/signup")
        suspend fun signUp(@RequestBody request: SignUpRequest) {
            userService.signUp(request)
        }
    }
    ```

* UserService (회원가입 api)

    ```
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
    ```
    💁 suspend 키워드는 비동기 방식을 수행할 때 사용하는 키워드이며 suspend 메서드를 사용하는 함수도 suspend 키워드가 붙어야 한다.<br/>
    💁 코틀린의 스코프 함수 let 을 이용해 signUpRequest 객체의 프로퍼티에 쉽게 접근할 수 있다.<br/>

* BCryptUtils <br/>

    ![image](https://github.com/sssun19/issue-service/assets/125242481/de54851b-5ae1-45b3-afa4-59843b479ce4) <br/>
    💁 BCrypt 를 사용해 매개변수로 받은 비밀번호를 해시된 문자열로 반환한다.


* UserService (로그인 api)
  
    ```
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
    ```
    💁 BCryptUtils 의 verify 메서드로 일반 비밀번호 문자와 해시된 문자열이 일치한지 검증한다. <br/>
    💁 비밀번호 검증이 완료되면 JWTClaim 객체를 생성하고 token 을 생성한다. 캐시 매니저에 유저 정보를 저장하고 응답을 내려준다.

* CoroutineCacheManager<T>

    ```
    private val localCache = ConcurrentHashMap<String, CacheWrapper<T>>()
    // 실무에서는 로컬 캐시가 아닌 redis 와 같은 캐시 매니저를 연동하는 경우가 많다.

    suspend fun awaitPut(key: String, value: T, ttl: Duration) {
        localCache[key] = CacheWrapper(cached = value, Instant.now().plusMillis(ttl.toMillis()))
    }
    ```
    💁 HashMap의 key(위에서는 token)를 이용해 생성한 JWTClaim 을 캐시에 저장하고 만료 시간을 설정. <br/>

    ```
    suspend fun awaitEvict(key: String) {
        localCache.remove(key)
    }
    ```
    💁 캐시에서 토큰 삭제하는 코드 <br/>

* ImageController <br/>
    ```
    @Controller // json 이 아닌 이미지 경로를 응답하는 컨트롤러이기 때문에 RestController 가 아닌 일반 Controller annotation을 사용
    @RequestMapping("/images")
    class ImageController {

        @GetMapping("{filename}")
        fun image(@PathVariable filename: String) : ResponseEntity<InputStreamResource> {
            val ext = filename.substring(filename.lastIndexOf("." +1))
            val file = File(ClassPathResource("/images/").file, filename)

            // images/jpg, images/png 등 업로드 확장자에 따라 헤더에 콘텐트 타입으로 응답을 내려준다.
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "images/$ext")
                .body(InputStreamResource(FileInputStream(file)))
            
        }
    }
    ```
    💁 변수 ext 는 파일의 확장자를 할당한다.
