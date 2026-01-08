package dev.sebastianrn.portfolioapp.data

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class ScrapedAsset(
    val id: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val weight: String,
    val buyPrice: String, // Price we pay to buy (User Buys)
    val sellPrice: String // Price we get if we sell (User Sells)
)

class PhiloroScrapingService {

    companion object {
        // Updated URL specifically for Gold Coins as requested
        private const val TARGET_URL = "https://philoro.ch/preisliste"
        private const val TAG = "PhiloroScraper"
    }

    fun scrapePrices(): List<ScrapedAsset> {
        val scrapedList = mutableListOf<ScrapedAsset>()

        try {
            Log.d(TAG, "Connecting to $TARGET_URL...")
            val doc: Document = Jsoup.connect(TARGET_URL)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(20000)
                .get()

            // Target the main price table body rows
            // The HTML structure is <table class="w-full text-left"> -> <tbody> -> <tr>
            val rows = doc.select("table tbody tr")

            Log.d(TAG, "Found ${rows.size} table rows. Parsing...")

            for (row in rows) {
                try {
                    // Get all cells (td) in the row
                    val cols = row.select("td")

                    // A valid product row in this specific table has 7 columns.
                    // We skip header rows like <tr><td colspan="7">Goldmünzen</td></tr>
                    if (cols.size < 7) {
                        continue
                    }

                    // --- 1. ID (Column 0) ---
                    val id = cols[0].text().trim()
                    if (id.isEmpty()) continue // Skip if no ID

                    // --- 2. IMAGE (Column 1) ---
                    // Look for <img> inside the second column
                    var imgUrl = cols[1].select("img").attr("src")
                    if (imgUrl.isNotEmpty() && !imgUrl.startsWith("http")) {
                        imgUrl = "https://philoro.ch$imgUrl" // Fix relative paths? Usually they are absolute CDN links
                    }

                    // --- 3. NAME (Column 2) ---
                    val name = cols[2].text().trim()

                    // --- 4. WEIGHT (Column 4) ---
                    val weight = cols[4].text().trim()

                    // --- 5. SELL PRICE (User Sells) (Column 5) ---
                    // This column contains the "Verkaufen" button and price
                    // Text format: "3.480,23 CHF"
                    val sellPriceText = cols[5].text()
                    val sellPrice = extractPrice(sellPriceText)

                    // --- 6. BUY PRICE (User Buys) (Column 6) ---
                    // This column contains the "Kaufen" button and price
                    val buyPriceText = cols[6].text()
                    val buyPrice = extractPrice(buyPriceText)

                    // Only add if we successfully parsed at least one price or name
                    if (name.isNotEmpty()) {
                        val asset = ScrapedAsset(
                            id = id,
                            name = name,
                            imageUrl = imgUrl,
                            description = "Weight: $weight",
                            weight = weight,
                            buyPrice = buyPrice,  // Price user pays to Buy
                            sellPrice = sellPrice // Price user gets to Sell
                        )
                        scrapedList.add(asset)
                        Log.d(TAG, "✅ SCRAPED: $id | $name | Buy: $buyPrice | Sell: $sellPrice")
                    }

                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing row: ${e.message}")
                }
            }

            Log.d(TAG, "Successfully scraped ${scrapedList.size} items.")

        } catch (e: Exception) {
            Log.e(TAG, "Error scraping website", e)
        }

        return scrapedList
    }

    // Helper to clean price strings (e.g., "3.480,23 CHF" -> "3480.23")
    private fun extractPrice(rawText: String): String {
        // Regex to find the number pattern like "1.234,56" or "123,45"
        val match = Regex("([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})").find(rawText)
        return match?.value?.replace(".", "")?.replace(",", ".") ?: "N/A"
    }
}