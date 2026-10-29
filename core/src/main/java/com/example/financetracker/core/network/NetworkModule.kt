package com.example.financetracker.core.network

import com.example.financetracker.core.BuildConfig
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://spendings-backend.t02.ru/"

// connect/read-таймауты не покрывают DNS: без сети запрос мог висеть бесконечно
private const val CALL_TIMEOUT_SECONDS = 20L

private val AUTH_INTERCEPTOR = named("authInterceptor")
private val LOGGING_INTERCEPTOR = named("loggingInterceptor")

/**
 * Общий сетевой слой. Feature-модули получают отсюда [Retrofit]
 * и создают из него свои API-интерфейсы.
 */
val networkModule = module {
    single { Gson() }

    single<Interceptor>(AUTH_INTERCEPTOR) {
        Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("Authorization", "Bearer ${BuildConfig.API_AUTH_TOKEN}")
                .build()
            chain.proceed(request)
        }
    }

    single<Interceptor>(LOGGING_INTERCEPTOR) {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
        }
    }

    single {
        OkHttpClient.Builder()
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(get<Interceptor>(AUTH_INTERCEPTOR))
            .addInterceptor(get<Interceptor>(LOGGING_INTERCEPTOR))
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }
}
