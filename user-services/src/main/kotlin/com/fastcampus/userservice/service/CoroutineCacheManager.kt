package com.fastcampus.userservice.service

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class CoroutineCacheManager<T> {

    private val localCache = ConcurrentHashMap<String, CacheWrapper<T>>()
    // 실무에서는 로컬 캐시가 아닌 레디스와 같은 캐시 매니저를 연동하는 경우가 많다.

    suspend fun awaitPut(key: String, value: T, ttl: Duration) {
        localCache[key] = CacheWrapper(cached = value, Instant.now().plusMillis(ttl.toMillis()))
    }

    suspend fun awaitEvict(key: String) {
        localCache.remove(key)
    }

    suspend fun awaitGetOrPut(
        key: String,
        ttl: Duration?= Duration.ofMinutes(5),
        supplier : suspend () -> T,
    ) : T {
        val now = Instant.now()
        val cacheWrapper = localCache[key]

        val cached = if (cacheWrapper == null) {
            CacheWrapper(cached = supplier(), ttl = now.plusMillis(ttl!!.toMillis())).also {
                localCache[key] = it
            }

        } else if(now.isAfter(cacheWrapper.ttl)) {
            // 캐시 ttl 이 지난 경우
            localCache.remove(key)
            CacheWrapper(cached = supplier(), ttl = now.plusMillis(ttl!!.toMillis())).also {
                localCache[key] = it
            }

        } else {
            cacheWrapper
        }

        checkNotNull(cached.cached)
        return cached.cached
    }


    data class CacheWrapper<T>(val cached: T, val ttl: Instant)

}