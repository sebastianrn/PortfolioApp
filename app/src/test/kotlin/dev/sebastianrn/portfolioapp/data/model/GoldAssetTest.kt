package dev.sebastianrn.portfolioapp.data.model

import dev.sebastianrn.portfolioapp.TestDataFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for GoldAsset computed properties.
 */
class GoldAssetTest {

    // --- totalCurrentValue Tests ---

    @Test
    fun `totalCurrentValue calculates correctly for single quantity`() {
        val asset = TestDataFactory.createGoldAsset(
            currentSellPrice = 1000.0,
            quantity = 1
        )

        assertEquals(1000.0, asset.totalCurrentValue, 0.001)
    }

    @Test
    fun `totalCurrentValue multiplies by quantity`() {
        val asset = TestDataFactory.createGoldAsset(
            currentSellPrice = 500.0,
            quantity = 5
        )

        assertEquals(2500.0, asset.totalCurrentValue, 0.001)
    }

    @Test
    fun `totalCurrentValue with zero quantity returns zero`() {
        val asset = TestDataFactory.createGoldAsset(
            currentSellPrice = 1000.0,
            quantity = 0
        )

        assertEquals(0.0, asset.totalCurrentValue, 0.001)
    }

    @Test
    fun `totalCurrentValue with zero price returns zero`() {
        val asset = TestDataFactory.createGoldAsset(
            currentSellPrice = 0.0,
            quantity = 5
        )

        assertEquals(0.0, asset.totalCurrentValue, 0.001)
    }

    @Test
    fun `totalCurrentValue handles large values`() {
        val asset = TestDataFactory.createGoldAsset(
            currentSellPrice = 999999.99,
            quantity = 100
        )

        assertEquals(99999999.0, asset.totalCurrentValue, 0.01)
    }

    @Test
    fun `totalCurrentValue handles decimal precision`() {
        val asset = TestDataFactory.createGoldAsset(
            currentSellPrice = 1234.56,
            quantity = 3
        )

        assertEquals(3703.68, asset.totalCurrentValue, 0.001)
    }

    // --- totalProfitOrLoss Tests ---

    @Test
    fun `totalProfitOrLoss calculates profit correctly`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 1000.0,
            currentSellPrice = 1200.0,
            quantity = 1
        )

        assertEquals(200.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss calculates loss correctly`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 1000.0,
            currentSellPrice = 800.0,
            quantity = 1
        )

        assertEquals(-200.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss with break even returns zero`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 1000.0,
            currentSellPrice = 1000.0,
            quantity = 1
        )

        assertEquals(0.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss multiplies by quantity for profit`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 100.0,
            currentSellPrice = 150.0,
            quantity = 4
        )

        // Profit = (150 - 100) * 4 = 200
        assertEquals(200.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss multiplies by quantity for loss`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 150.0,
            currentSellPrice = 100.0,
            quantity = 4
        )

        // Loss = (100 - 150) * 4 = -200
        assertEquals(-200.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss with zero quantity returns zero`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 1000.0,
            currentSellPrice = 1500.0,
            quantity = 0
        )

        assertEquals(0.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss with zero purchase price`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 0.0,
            currentSellPrice = 100.0,
            quantity = 1
        )

        // Profit = 100 - 0 = 100 (free asset appreciated)
        assertEquals(100.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss with zero current price`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 100.0,
            currentSellPrice = 0.0,
            quantity = 1
        )

        // Loss = 0 - 100 = -100 (total loss)
        assertEquals(-100.0, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss handles decimal precision`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 1234.56,
            currentSellPrice = 1345.67,
            quantity = 2
        )

        // Profit = (1345.67 - 1234.56) * 2 = 222.22
        assertEquals(222.22, asset.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `totalProfitOrLoss handles large values`() {
        val asset = TestDataFactory.createGoldAsset(
            purchasePrice = 100000.0,
            currentSellPrice = 100100.0,
            quantity = 1000
        )

        // Profit = (100100 - 100000) * 1000 = 100000
        assertEquals(100000.0, asset.totalProfitOrLoss, 0.01)
    }

    // --- Data Class Tests ---

    @Test
    fun `GoldAsset equality works correctly`() {
        val asset1 = TestDataFactory.createGoldAsset(id = 1, name = "Test")
        val asset2 = TestDataFactory.createGoldAsset(id = 1, name = "Test")

        assertEquals(asset1, asset2)
    }

    @Test
    fun `GoldAsset copy preserves computed properties`() {
        val original = TestDataFactory.createGoldAsset(
            purchasePrice = 100.0,
            currentSellPrice = 150.0,
            quantity = 2
        )
        val copy = original.copy(quantity = 3)

        // Original: value = 300, profit = 100
        assertEquals(300.0, original.totalCurrentValue, 0.001)
        assertEquals(100.0, original.totalProfitOrLoss, 0.001)

        // Copy: value = 450, profit = 150
        assertEquals(450.0, copy.totalCurrentValue, 0.001)
        assertEquals(150.0, copy.totalProfitOrLoss, 0.001)
    }

    // --- AssetType Tests ---

    @Test
    fun `COIN type asset calculates correctly`() {
        val coin = TestDataFactory.createGoldCoin(
            purchasePrice = 500.0,
            currentSellPrice = 600.0,
            quantity = 2
        )

        assertEquals(AssetType.COIN, coin.type)
        assertEquals(1200.0, coin.totalCurrentValue, 0.001)
        assertEquals(200.0, coin.totalProfitOrLoss, 0.001)
    }

    @Test
    fun `BAR type asset calculates correctly`() {
        val bar = TestDataFactory.createGoldBar(
            purchasePrice = 5000.0,
            currentSellPrice = 5500.0,
            quantity = 1
        )

        assertEquals(AssetType.BAR, bar.type)
        assertEquals(5500.0, bar.totalCurrentValue, 0.001)
        assertEquals(500.0, bar.totalProfitOrLoss, 0.001)
    }
}
