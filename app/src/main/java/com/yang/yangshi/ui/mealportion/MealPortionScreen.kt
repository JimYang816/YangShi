package com.yang.yangshi.ui.mealportion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yang.yangshi.domain.calculator.FoodPortionInput
import com.yang.yangshi.domain.model.FoodItem
import com.yang.yangshi.domain.model.MealType

@Composable
fun MealPortionScreen(
    viewModel: MealPortionViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "食物配平与打卡计算器",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. 餐别选择器
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. 选择打卡餐别",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { mealType ->
                        FilterChip(
                            selected = state.selectedMealType == mealType,
                            onClick = { viewModel.setMealType(mealType) },
                            label = { Text(mealType.displayName) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                val target = state.mealTarget
                Text(
                    text = "${state.selectedMealType.displayName}目标: 碳 ${target.carbsGrams}g | 蛋 ${target.proteinGrams}g | 脂 ${target.fatGrams}g (${target.caloriesKcal.toInt()} kcal)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 2. 食物搜索 (中国食物成分表 API)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. 搜索中国食物成分表 (chinanutri.cn)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        label = { Text("例如：米饭、鸡胸肉、苹果") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(onClick = viewModel::searchFood) {
                        Text(if (state.isSearching) "搜索中..." else "搜索")
                    }
                }

                if (state.searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "搜索结果 (点击加入计算):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    state.searchResults.forEach { food ->
                        SearchResultItem(
                            food = food,
                            onAddClick = { viewModel.addFoodToCalculation(food) }
                        )
                    }
                }
            }
        }

        // 3. 已选食物与自动配平面板
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. 多食物自动配平面板",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (state.selectedFoodInputs.isEmpty()) {
                    Text(
                        text = "暂未添加食物，请在上方搜索并添加食物参与自动配平。",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    state.selectedFoodInputs.forEachIndexed { index, input ->
                        SelectedFoodInputRow(
                            index = index,
                            input = input,
                            onRemove = { viewModel.removeFoodFromCalculation(index) },
                            onToggleLock = { isLocked, fixedWeight ->
                                viewModel.toggleLockFood(index, isLocked, fixedWeight)
                            }
                        )
                    }

                    state.calculationResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "自动计算推荐重量结果:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        result.portions.forEach { portion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "• ${portion.foodName} ${if (portion.isLocked) "[已锁]" else ""}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "推荐 ${portion.weightGrams}g (${portion.actualCalories.toInt()} kcal)",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "实际总计: 碳 ${result.totalActual.carbsGrams}g | 蛋 ${result.totalActual.proteinGrams}g | 脂 ${result.totalActual.fatGrams}g (${result.totalActual.caloriesKcal.toInt()} kcal)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "目标差距 (目标 - 实际): 碳 ${result.delta.carbsGrams}g | 蛋 ${result.delta.proteinGrams}g | 脂 ${result.delta.fatGrams}g",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }

        state.errorMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        state.addSuccessMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = viewModel::addToMealLog,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = state.calculationResult != null && state.calculationResult!!.portions.isNotEmpty()
        ) {
            Text(text = "添加至 ${state.selectedMealType.displayName} 打卡日志", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchResultItem(
    food: FoodItem,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = food.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                text = "每100g: 碳 ${food.carbsG}g | 蛋 ${food.proteinG}g | 脂 ${food.fatG}g (${food.energyKcal.toInt()} kcal)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        OutlinedButton(onClick = onAddClick) {
            Text("+ 配平")
        }
    }
}

@Composable
private fun SelectedFoodInputRow(
    index: Int,
    input: FoodPortionInput,
    onRemove: () -> Unit,
    onToggleLock: (Boolean, Double?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "${index + 1}. ${input.density.name}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            OutlinedButton(onClick = onRemove) {
                Text("删除", fontSize = 12.sp)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = input.isLocked,
                onCheckedChange = { isChecked ->
                    onToggleLock(isChecked, if (isChecked) 100.0 else null)
                }
            )
            Text(text = "固定重量 (g):", fontSize = 13.sp)
            if (input.isLocked) {
                OutlinedTextField(
                    value = (input.fixedWeightGrams ?: 100.0).toString(),
                    onValueChange = { weightStr ->
                        val weight = weightStr.toDoubleOrNull() ?: 0.0
                        onToggleLock(true, weight)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp),
                    singleLine = true
                )
            } else {
                Text(text = "[自动计算未锁定]", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
