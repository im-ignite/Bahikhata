package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun weightBasedPriceCalculation_isAccurate() {
    val weightKg = 34.5
    val pricePerKg = 12.0
    val expectedPrice = 414.0
    val calculatedPrice = weightKg * pricePerKg
    assertEquals(expectedPrice, calculatedPrice, 0.001)
  }

  @Test
  fun inventoryDeduction_calculatesCorrectly() {
    val initialStockPieces = 100
    val initialStockWeightKg = 250.0

    val salePieces = 15
    val saleWeightKg = 42.5

    val remainingPieces = initialStockPieces - salePieces
    val remainingWeightKg = initialStockWeightKg - saleWeightKg

    assertEquals(85, remainingPieces)
    assertEquals(207.5, remainingWeightKg, 0.001)
  }
}
