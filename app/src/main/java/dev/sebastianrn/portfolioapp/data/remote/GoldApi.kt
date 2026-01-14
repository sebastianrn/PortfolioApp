package dev.sebastianrn.portfolioapp.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

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
    private const val BASE_URL = "https://www.goldapi.io/"

    val api: GoldApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoldApiService::class.java)
    }
}