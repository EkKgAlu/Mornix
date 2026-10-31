package com.example.data.remote

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ApiClient {
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mornix/1.0 (Android; Live Morning Briefing; Reliable Data Engine)")
                    .header("Accept", "application/json, application/xml, text/xml, */*")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
}
