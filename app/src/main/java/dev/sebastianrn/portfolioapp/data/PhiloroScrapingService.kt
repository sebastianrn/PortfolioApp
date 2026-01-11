package dev.sebastianrn.portfolioapp.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.URL

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
    val buyPrice: String,
    val sellPrice: String
)


class PhiloroScrapingService {

    companion object {
        private const val API_BASE_URL = "https://philoro.ch/api/prices/products?country=CH&currency=CHF&skus="
        private const val TAG = "PhiloroApiService"
    }

    /**
     * Fetches current prices for the specific list of Philoro IDs (SKUs).
     * @param skus List of IDs (e.g., ["1991", "2000"])
     */
    fun fetchPrices(skus: List<String>): List<ScrapedAsset> {
        if (skus.isEmpty()) return emptyList()

        val scrapedList = mutableListOf<ScrapedAsset>()

        try {
            // 1. Construct URL with comma-separated SKUs
            val skuParam = skus.joinToString(",")
            val fullUrl = "$API_BASE_URL$skuParam"

            Log.d(TAG, "Calling API: $fullUrl")

            // 2. Fetch JSON
            val jsonString = URL(fullUrl).readText()

            // 3. Parse JSON
            val response = Gson().fromJson(jsonString, ApiResponse::class.java)

            // 4. Map to ScrapedAsset
            for (product in response.products) {
                // Find "User Buys" price (In JSON: type="buy" usually means Dealer Sells to User)
                // Find "User Sells" price (In JSON: type="sell" usually means Dealer Buys from User)

                // Note based on your JSON:
                // "sell" centAmount: "356008" (3560 CHF) -> Lower -> Dealer Buys (User Sells)
                // "buy" centAmount: "378873" (3788 CHF) -> Higher -> Dealer Sells (User Buys)

                val userSellPriceRaw = product.prices.find { it.type == "sell" }?.centAmount?.toDoubleOrNull() ?: 0.0
                val userBuyPriceRaw = product.prices.find { it.type == "buy" }?.centAmount?.toDoubleOrNull() ?: 0.0

                // Convert Cents to CHF
                val userSellPrice = userSellPriceRaw / 100.0
                val userBuyPrice = userBuyPriceRaw / 100.0

                if (userBuyPrice > 0) {
                    scrapedList.add(
                        ScrapedAsset(
                            id = product.sku,
                            name = product.name,
                            description = "Weight: ${product.weight}",
                            weight = product.weight,
                            buyPrice = userBuyPrice.toString(),
                            sellPrice = userSellPrice.toString()
                        )
                    )
                    Log.d(TAG, "✅ API SUCCESS: ${product.name} | Sell: $userSellPrice | Buy: $userBuyPrice")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "API Request failed: ${e.message}", e)
        }

        return scrapedList
    }
}