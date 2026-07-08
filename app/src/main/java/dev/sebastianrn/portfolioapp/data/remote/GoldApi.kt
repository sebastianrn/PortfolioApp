package dev.sebastianrn.portfolioapp.data.remote

import dev.sebastianrn.portfolioapp.util.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

data class GoldPriceResponse(
    val price: Double,
    val price_gram_24k: Double,
    val price_gram_22k: Double,
    val price_gram_21k: Double,
    val price_gram_20k: Double,
    val price_gram_18k: Double
)

interface GoldApiService {
    @GET("api/XAU/{currency}")
    suspend fun getGoldPrice(
        @Path("currency") currency: String,
        @Header("x-access-token") apiKey: String
    ): GoldPriceResponse
}

object NetworkModule {

    /** Shared HTTP client with sane timeouts for all network calls. */
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val api: GoldApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.GOLD_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoldApiService::class.java)
    }
}