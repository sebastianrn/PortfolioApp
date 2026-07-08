package dev.sebastianrn.portfolioapp.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dev.sebastianrn.portfolioapp.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class ApiResponse(
    @SerializedName("products") val products: List<ApiProduct>
)

data class ApiProduct(
    @SerializedName("sku") val sku: String,
    @SerializedName("name") val name: String,
    @SerializedName("weight") val weight: String,
    @SerializedName("prices") val prices: List<ApiPrice>
)

data class ApiPrice(
    @SerializedName("type") val type: String,
    @SerializedName("centAmount") val centAmount: String,
    @SerializedName("currency") val currency: String
)

data class ScrapedAsset(
    val id: String,
    val name: String,
    val description: String,
    val weight: String,
    val buyPrice: Double,
    val sellPrice: Double
)

class PhiloroScrapingService(
    private val client: OkHttpClient = NetworkModule.okHttpClient
) {

    companion object {
        private const val TAG = "PhiloroApiService"
    }

    /**
     * Fetches current prices for the specific list of Philoro IDs (SKUs).
     * Network or parsing failures propagate as exceptions so callers can
     * surface the real error.
     *
     * @param skus List of IDs (e.g., ["1991", "2000"])
     */
    suspend fun fetchPrices(skus: List<String>): List<ScrapedAsset> {
        if (skus.isEmpty()) return emptyList()

        return withContext(Dispatchers.IO) {
            val fullUrl = "${Constants.PHILORO_API_BASE_URL}${skus.joinToString(",")}"
            Log.d(TAG, "Calling API: $fullUrl")

            val request = Request.Builder().url(fullUrl).build()
            val jsonString = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Philoro API returned HTTP ${response.code}")
                }
                response.body?.string() ?: throw IOException("Philoro API returned an empty body")
            }

            val apiResponse = Gson().fromJson(jsonString, ApiResponse::class.java)
                ?: throw IOException("Philoro API returned unparseable data")

            apiResponse.products.mapNotNull { product ->
                // In the Philoro JSON, type "buy" is the dealer's sell price (user buys,
                // higher) and type "sell" is the dealer's buy price (user sells, lower).
                val userSellPrice = product.prices.centAmount("sell")
                val userBuyPrice = product.prices.centAmount("buy")

                if (userBuyPrice > 0) {
                    ScrapedAsset(
                        id = product.sku,
                        name = product.name,
                        description = "Weight: ${product.weight}",
                        weight = product.weight,
                        buyPrice = userBuyPrice,
                        sellPrice = userSellPrice
                    )
                } else {
                    null
                }
            }
        }
    }

    private fun List<ApiPrice>.centAmount(type: String): Double =
        (find { it.type == type }?.centAmount?.toDoubleOrNull() ?: 0.0) / 100.0
}
