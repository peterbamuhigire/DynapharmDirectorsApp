package com.dynapharm.owner.data.remote.interceptor

import com.dynapharm.owner.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Interceptor that adds Host header for virtual host routing in dev mode.
 *
 * Required when accessing WAMP virtual hosts via IP address
 * instead of hostname (e.g., from Android emulator).
 *
 * Only active in dev flavor - staging/prod use real domain names.
 */
class HostHeaderInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Only add Host header in dev mode (accessing via IP)
        val newRequest = if (BuildConfig.API_BASE_URL.contains("192.168.")) {
            request.newBuilder()
                .header("Host", "dynapharm.peter")
                .build()
        } else {
            request
        }

        return chain.proceed(newRequest)
    }
}
