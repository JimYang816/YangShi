package com.yang.yangshi.domain.calculator

import kotlin.math.abs
import kotlin.math.max

/**
 * 食物营养密度 (每 100g 可食部食物中的营养成分)
 */
data class FoodNutrientDensity(
    val foodId: Long,
    val name: String,
    val carbsPer100g: Double,
    val proteinPer100g: Double,
    val fatPer100g: Double,
    val edibleRatio: Double = 1.0
) {
    val caloriesPer100g: Double
        get() = carbsPer100g * MacroConstants.KCAL_PER_G_CARBS +
                proteinPer100g * MacroConstants.KCAL_PER_G_PROTEIN +
                fatPer100g * MacroConstants.KCAL_PER_G_FAT
}

/**
 * 食物配平输入参数
 */
data class FoodPortionInput(
    val density: FoodNutrientDensity,
    val isLocked: Boolean = false,
    val fixedWeightGrams: Double? = null,
    val priorityMacro: MacroType? = null
)

/**
 * 单个食物的计算推荐结果
 */
data class FoodPortionResult(
    val foodId: Long,
    val foodName: String,
    val weightGrams: Double,
    val actualCarbs: Double,
    val actualProtein: Double,
    val actualFat: Double,
    val actualCalories: Double,
    val isLocked: Boolean
)

/**
 * 多食物配平总体输出结果
 */
data class MultiPortionResult(
    val portions: List<FoodPortionResult>,
    val target: MacroTarget,
    val totalActual: MacroTarget,
    val delta: MacroTarget // 目标与实际差距 (Target - Actual)
)

/**
 * 食物重量推荐与配平引擎
 */
object FoodPortionAllocationEngine {

    /**
     * 单食物重量推荐计算
     */
    fun calculateSingleFoodPortion(
        targetGrams: Double,
        targetMacro: MacroType,
        density: FoodNutrientDensity
    ): FoodPortionResult? {
        val densityValue = when (targetMacro) {
            MacroType.CARBS -> density.carbsPer100g
            MacroType.PROTEIN -> density.proteinPer100g
            MacroType.FAT -> density.fatPer100g
            MacroType.CALORIES -> density.caloriesPer100g
        }

        if (densityValue <= 0.0) return null

        val weightGrams = (targetGrams / densityValue) * 100.0
        val factor = weightGrams / 100.0

        val actualCarbs = density.carbsPer100g * factor
        val actualProtein = density.proteinPer100g * factor
        val actualFat = density.fatPer100g * factor
        val actualCalories = density.caloriesPer100g * factor

        return FoodPortionResult(
            foodId = density.foodId,
            foodName = density.name,
            weightGrams = MacroTargetCalculator.roundToOneDecimal(weightGrams),
            actualCarbs = MacroTargetCalculator.roundToOneDecimal(actualCarbs),
            actualProtein = MacroTargetCalculator.roundToOneDecimal(actualProtein),
            actualFat = MacroTargetCalculator.roundToOneDecimal(actualFat),
            actualCalories = MacroTargetCalculator.roundToInteger(actualCalories),
            isLocked = false
        )
    }

    /**
     * 多食物组合配平求解器 (NNLS 保证非负解)
     */
    fun calculateMultiFoodPortions(
        mealTarget: MacroTarget,
        inputs: List<FoodPortionInput>
    ): MultiPortionResult {
        if (inputs.isEmpty()) {
            return MultiPortionResult(
                portions = emptyList(),
                target = mealTarget,
                totalActual = MacroTarget(0.0, 0.0, 0.0, 0.0),
                delta = mealTarget
            )
        }

        var remCarbs = mealTarget.carbsGrams
        var remProtein = mealTarget.proteinGrams
        var remFat = mealTarget.fatGrams

        val lockedPortions = mutableMapOf<Int, FoodPortionResult>()
        val freeIndices = mutableListOf<Int>()

        inputs.forEachIndexed { index, input ->
            if (input.isLocked && input.fixedWeightGrams != null && input.fixedWeightGrams >= 0) {
                val w = input.fixedWeightGrams
                val factor = w / 100.0
                val c = input.density.carbsPer100g * factor
                val p = input.density.proteinPer100g * factor
                val f = input.density.fatPer100g * factor
                val cal = input.density.caloriesPer100g * factor

                remCarbs -= c
                remProtein -= p
                remFat -= f

                lockedPortions[index] = FoodPortionResult(
                    foodId = input.density.foodId,
                    foodName = input.density.name,
                    weightGrams = w,
                    actualCarbs = c,
                    actualProtein = p,
                    actualFat = f,
                    actualCalories = cal,
                    isLocked = true
                )
            } else {
                freeIndices.add(index)
            }
        }

        val freeWeights100g = solveNonNegativeLeastSquares(
            targetCarbs = max(0.0, remCarbs),
            targetProtein = max(0.0, remProtein),
            targetFat = max(0.0, remFat),
            freeInputs = freeIndices.map { inputs[it] }
        )

        val finalPortions = mutableListOf<FoodPortionResult>()
        var totalActualCarbs = 0.0
        var totalActualProtein = 0.0
        var totalActualFat = 0.0

        inputs.indices.forEach { index ->
            if (lockedPortions.containsKey(index)) {
                val locked = lockedPortions[index]!!
                finalPortions.add(locked)
                totalActualCarbs += locked.actualCarbs
                totalActualProtein += locked.actualProtein
                totalActualFat += locked.actualFat
            } else {
                val freeLocalIndex = freeIndices.indexOf(index)
                val w100g = freeWeights100g[freeLocalIndex]
                val weightGrams = MacroTargetCalculator.roundToOneDecimal(w100g * 100.0)
                val factor = weightGrams / 100.0
                val density = inputs[index].density

                val c = density.carbsPer100g * factor
                val p = density.proteinPer100g * factor
                val f = density.fatPer100g * factor
                val cal = density.caloriesPer100g * factor

                val portion = FoodPortionResult(
                    foodId = density.foodId,
                    foodName = density.name,
                    weightGrams = weightGrams,
                    actualCarbs = MacroTargetCalculator.roundToOneDecimal(c),
                    actualProtein = MacroTargetCalculator.roundToOneDecimal(p),
                    actualFat = MacroTargetCalculator.roundToOneDecimal(f),
                    actualCalories = MacroTargetCalculator.roundToInteger(cal),
                    isLocked = false
                )
                finalPortions.add(portion)
                totalActualCarbs += portion.actualCarbs
                totalActualProtein += portion.actualProtein
                totalActualFat += portion.actualFat
            }
        }

        val totalActualCalories = totalActualCarbs * MacroConstants.KCAL_PER_G_CARBS +
                totalActualProtein * MacroConstants.KCAL_PER_G_PROTEIN +
                totalActualFat * MacroConstants.KCAL_PER_G_FAT

        val totalActual = MacroTarget(
            carbsGrams = MacroTargetCalculator.roundToOneDecimal(totalActualCarbs),
            proteinGrams = MacroTargetCalculator.roundToOneDecimal(totalActualProtein),
            fatGrams = MacroTargetCalculator.roundToOneDecimal(totalActualFat),
            caloriesKcal = MacroTargetCalculator.roundToInteger(totalActualCalories)
        )

        val delta = MacroTarget(
            carbsGrams = MacroTargetCalculator.roundToOneDecimal(mealTarget.carbsGrams - totalActual.carbsGrams),
            proteinGrams = MacroTargetCalculator.roundToOneDecimal(mealTarget.proteinGrams - totalActual.proteinGrams),
            fatGrams = MacroTargetCalculator.roundToOneDecimal(mealTarget.fatGrams - totalActual.fatGrams),
            caloriesKcal = MacroTargetCalculator.roundToInteger(mealTarget.caloriesKcal - totalActual.caloriesKcal)
        )

        return MultiPortionResult(
            portions = finalPortions,
            target = mealTarget,
            totalActual = totalActual,
            delta = delta
        )
    }

    private fun solveNonNegativeLeastSquares(
        targetCarbs: Double,
        targetProtein: Double,
        targetFat: Double,
        freeInputs: List<FoodPortionInput>
    ): DoubleArray {
        val k = freeInputs.size
        if (k == 0) return DoubleArray(0)

        val a = Array(3) { DoubleArray(k) }
        freeInputs.forEachIndexed { j, input ->
            a[0][j] = input.density.carbsPer100g
            a[1][j] = input.density.proteinPer100g
            a[2][j] = input.density.fatPer100g
        }
        val b = doubleArrayOf(targetCarbs, targetProtein, targetFat)

        var bestX = DoubleArray(k)
        var minResidual = Double.MAX_VALUE

        val totalSubsets = 1 shl k
        for (mask in 0 until totalSubsets) {
            val freeVars = mutableListOf<Int>()
            for (i in 0 until k) {
                if ((mask and (1 shl i)) != 0) {
                    freeVars.add(i)
                }
            }

            if (freeVars.isEmpty()) {
                val residual = b[0] * b[0] + b[1] * b[1] + b[2] * b[2]
                if (residual < minResidual) {
                    minResidual = residual
                    bestX = DoubleArray(k)
                }
                continue
            }

            val subX = solveLeastSquaresSubproblem(a, b, freeVars)
            if (subX != null && subX.all { it >= -1e-6 }) {
                val candidateX = DoubleArray(k)
                freeVars.forEachIndexed { idx, varIdx ->
                    candidateX[varIdx] = max(0.0, subX[idx])
                }

                val resC = b[0] - freeVars.sumOf { a[0][it] * candidateX[it] }
                val resP = b[1] - freeVars.sumOf { a[1][it] * candidateX[it] }
                val resF = b[2] - freeVars.sumOf { a[2][it] * candidateX[it] }
                val residual = resC * resC + resP * resP + resF * resF

                if (residual < minResidual) {
                    minResidual = residual
                    bestX = candidateX
                }
            }
        }

        return bestX
    }

    private fun solveLeastSquaresSubproblem(
        a: Array<DoubleArray>,
        b: DoubleArray,
        freeVars: List<Int>
    ): DoubleArray? {
        val m = freeVars.size
        val ata = Array(m) { DoubleArray(m) }
        val atb = DoubleArray(m)

        for (i in 0 until m) {
            val colI = freeVars[i]
            for (j in 0 until m) {
                val colJ = freeVars[j]
                var sum = 0.0
                for (row in 0..2) {
                    sum += a[row][colI] * a[row][colJ]
                }
                ata[i][j] = sum
            }
            var sumB = 0.0
            for (row in 0..2) {
                sumB += a[row][colI] * b[row]
            }
            atb[i] = sumB
        }

        return solveSymmetricSystem(ata, atb)
    }

    private fun solveSymmetricSystem(matrix: Array<DoubleArray>, b: DoubleArray): DoubleArray? {
        val n = b.size
        val aug = Array(n) { i -> DoubleArray(n + 1) { j -> if (j < n) matrix[i][j] else b[i] } }

        for (i in 0 until n) {
            var maxRow = i
            for (k in i + 1 until n) {
                if (abs(aug[k][i]) > abs(aug[maxRow][i])) maxRow = k
            }
            val temp = aug[i]
            aug[i] = aug[maxRow]
            aug[maxRow] = temp

            if (abs(aug[i][i]) < 1e-9) return null

            for (k in i + 1 until n) {
                val factor = aug[k][i] / aug[i][i]
                for (j in i until n + 1) {
                    aug[k][j] -= factor * aug[i][j]
                }
            }
        }

        val x = DoubleArray(n)
        for (i in n - 1 downTo 0) {
            var sum = aug[i][n]
            for (j in i + 1 until n) {
                sum -= aug[i][j] * x[j]
            }
            x[i] = sum / aug[i][i]
        }
        return x
    }
}
