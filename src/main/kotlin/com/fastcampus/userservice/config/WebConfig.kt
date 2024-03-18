package com.fastcampus.userservice.config

import com.fastcampus.userservice.model.AuthToken
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Configuration
class WebConfig(
    private val authTokenResolver: AuthTokenResolver,
) :WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        super.configureArgumentResolvers(configurer)
        configurer.addCustomResolver(authTokenResolver)
    } // 하단에서 만든 AuthTokenResolver 를 configuration 으로 주입하는 코드.

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .maxAge(3600)
    }
}

@Component
class AuthTokenResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean { // 어떤 조건이 동작하는 경우에만 resolver 가 동작
        return parameter.hasParameterAnnotation(AuthToken::class.java) // @AuthToken annotation 이 동작하는 경우에는 true 반환하고 resolveArgument 동작
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {

        val authHeader = exchange.request.headers["Authorization"]?.first()
        checkNotNull(authHeader) // 이 파트를 지나면 authHeader 를 notnull로 판단

        val token = authHeader.split(" ")[1]
        // JWT토큰의 경우 header 가 들어가면 Bearer 식별자를 통해 들어가게 되고 한 칸 뛰고 토큰이 들어가게 됨. 그 공백을 기준으로 1번인덱스 (토큰)만 가져오는 동작.
        return token.toMono()

    }

}