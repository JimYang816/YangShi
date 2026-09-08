package com.yang.yangshi.domain.model

import com.yang.yangshi.domain.calculator.MacroTargetCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class UserProfileTest {

    @Test
    fun testBmiCalculation() {
        val profile = UserProfile(
            heightCm = 180.0,
            weightKg = 72.0
        )
        // BMI = 72 / (1.8 * 1.8) = 72 / 3.24 = 22.222...
        assertEquals(22.2222, profile.bmi, 0.001)
    }

    @Test
    fun testDailyTargetCalculation() {
        val profile = UserProfile(
            heightCm = 175.0,
            weightKg = 70.0,
            carbGPerKg = 3.0,
            proteinGPerKg = 2.0,
            fatGPerKg = 0.8
        )
        val target = MacroTargetCalculator.calculateDailyTarget(profile)
        assertEquals(210.0, target.carbsGrams, 0.01)
        assertEquals(140.0, target.proteinGrams, 0.01)
        assertEquals(56.0, target.fatGrams, 0.01)
        assertEquals(1904.0, target.caloriesKcal, 0.01)
    }
}
