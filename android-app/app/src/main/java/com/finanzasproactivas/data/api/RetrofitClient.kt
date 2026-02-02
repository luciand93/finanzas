package com.finanzasproactivas.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit para comunicarse con el backend de Vercel.
 * La URL se toma de BuildConfig.API_BASE_URL (definida en app/build.gradle).
 * Si obtienes "unable to resolve host", cambia API_BASE_URL en build.gradle por una URL que funcione en tu red.
 */
object RetrofitClient {
    
    private val BASE_URL: String = com.finanzasproactivas.BuildConfig.API_BASE_URL
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    val apiService: FinanzasApiService = retrofit.create(FinanzasApiService::class.java)
}
