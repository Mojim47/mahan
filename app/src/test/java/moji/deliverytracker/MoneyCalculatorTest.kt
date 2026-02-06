package moji.deliverytracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MoneyCalculatorTest {

    @Test
    fun commissionAmount_knownValues() {
        assertEquals(0, MoneyCalculator.commissionAmount(0, 10f))
        assertEquals(0, MoneyCalculator.commissionAmount(-100, 10f))
        assertEquals(10, MoneyCalculator.commissionAmount(100, 10f))
        assertEquals(33, MoneyCalculator.commissionAmount(100, 33.3f))
        assertEquals(50, MoneyCalculator.commissionAmount(100, 50f))
        assertEquals(125, MoneyCalculator.commissionAmount(500, 25f))
    }

    @Test
    fun commissionAmount_randomizedProperties() {
        val random = Random(42)
        repeat(1000) {
            val total = random.nextInt(-1000, 2_000_000)
            val percent = random.nextDouble(-50.0, 150.0).toFloat()

            val commission = MoneyCalculator.commissionAmount(total, percent)
            val net = MoneyCalculator.netIncome(total, percent)

            if (total <= 0 || percent <= 0f) {
                assertEquals(0, commission)
            } else {
                assertTrue(commission >= 0)
                assertTrue(commission <= total)
            }
            assertEquals(total - commission, net)
        }
    }

    @Test
    fun balance_isNetMinusPaid() {
        assertEquals(0, MoneyCalculator.balance(100, 100))
        assertEquals(50, MoneyCalculator.balance(100, 50))
        assertEquals(-20, MoneyCalculator.balance(80, 100))
    }
}
