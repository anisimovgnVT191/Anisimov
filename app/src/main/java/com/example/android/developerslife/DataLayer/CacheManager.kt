package com.example.android.developerslife.DataLayer

import android.util.LruCache
import com.example.android.developerslife.DataLayer.MainFeature.DataModels.DevsLifeResponse

object CacheManager {
    private const val cacheSize = 25 * 1024 * 1024

    private val cache: LruCache<String, DevsLifeResponse> = LruCache(cacheSize)

    fun isCached(key: String) = cache.get(key)?.let { true } ?: false

    fun get(key: String): DevsLifeResponse? = cache.get(key)

    fun put(key: String, data: DevsLifeResponse) { cache.put(key, data) }

    fun clearCache() { cache.evictAll() }
}
