# issue-service

### issue-service / user-service 멀티 모듈로 이슈 관리 api 개발

🧑‍🏫 유저 정보를 JWT 토큰으로 관리하기

- 의존성 추가

![image](https://github.com/sssun19/issue-service/assets/125242481/9f43936a-454c-4c69-b9d2-51aca6f631eb)

- JWTUtils<br/>

👀 JWT (Json Web Token) 는 클레임 claim 과 서명 signature 객체를 가지고 있다. 클레임 객체는 유저의 아이디(식별자), 이메일, 프로필사진, 유저 이름 정보를 가지고 있다. <br/>

![image](https://github.com/sssun19/issue-service/assets/125242481/110f3c31-7afa-4ca0-8f48-8d2cbd0b668d)

👀 JWTProperties 객체는 issuer (토큰 발급자), subject, expiresTime (토큰 만료 시간), secret (키) 정보를 포함한다. <br/>

![image](https://github.com/sssun19/issue-service/assets/125242481/4ee7409f-5ad0-400c-88de-9c5d52ae2df4)

> **ConstructorBinding** : 해당 클래스의 프로퍼티를 final 로 선언하며, 값은 생성자를 통해 주입된다. 객체가 생성된 후에는 값을 변경할 수 없어 객체의 불변성을 보장. <br/>

> **ConfigurationProperties** : 외부 설정 파일(application.yml)에서 prefix = "jwt" 로 지정한 부분을 읽어와 매핑.

![image](https://github.com/sssun19/issue-service/assets/125242481/92cdf724-dac9-4388-916a-7b6a0ffc1c56)





#### ✨issue-service<br/>
* IssueController

```
@PostMapping
fun create(
    authUser: AuthUser,
    @RequestBody request: IssueRequest,
) = issueService.create(authUser.userId, request)
```
