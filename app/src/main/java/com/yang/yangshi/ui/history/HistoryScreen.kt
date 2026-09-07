package com.yang.yangshi.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yang.yangshi.domain.model.MealLog
import com.yang.yangshi.domain.model.MealLogItem
import com.yang.yangshi.domain.model.MealType

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
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
            text = "历史打卡日志与细目",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // 1. 日期切换 Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = viewModel::navigatePreviousDay) {
                    Text("< 前一天")
                }
                Text(
                    text = state.selectedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = viewModel::navigateNextDay) {
                    Text("后一天 >")
                }
            }
        }

        // 2. 当日总摄入 vs 目标汇总卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${state.selectedDate} 营养摄入总计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "实际摄入:", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "碳 ${state.totalConsumed.carbsGrams}g | 蛋 ${state.totalConsumed.proteinGrams}g | 脂 ${state.totalConsumed.fatGrams}g (${state.totalConsumed.caloriesKcal.toInt()} kcal)",
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "每日目标:", color = MaterialTheme.colorScheme.outline)
                    Text(
                        text = "碳 ${state.dailyTarget.carbsGrams}g | 蛋 ${state.dailyTarget.proteinGrams}g | 脂 ${state.dailyTarget.fatGrams}g (${state.dailyTarget.caloriesKcal.toInt()} kcal)",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // 3. 按餐别折叠/展开的明细列表 (四餐)
        MealType.entries.forEach { mealType ->
            val logForMeal = state.mealLogs.find { it.mealType == mealType }
            val isExpanded = state.expandedMealTypes.contains(mealType)

            ExpandableMealCard(
                mealType = mealType,
                mealLog = logForMeal,
                isExpanded = isExpanded,
                onToggleExpand = { viewModel.toggleMealExpanded(mealType) },
                onDeleteItem = viewModel::deleteLogItem
            )
        }
    }
}

@Composable
private fun ExpandableMealCard(
    mealType: MealType,
    mealLog: MealLog?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = mealType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (mealLog != null && mealLog.items.isNotEmpty()) {
                        Text(
                            text = "已记 ${mealLog.items.size} 项 (共 ${mealLog.totalActualEnergy.toInt()} kcal)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "无记录",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Text(
                    text = if (isExpanded) "收起 ▲" else "展开 ▼",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                if (mealLog == null || mealLog.items.isEmpty()) {
                    Text(
                        text = "该餐别暂无记录",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    mealLog.items.forEach { item ->
                        HistoryLogItemRow(
                            item = item,
                            onDelete = { onDeleteItem(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryLogItemRow(
    item: MealLogItem,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${item.foodName} (${item.foodWeightG}g)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = "碳 ${item.actualCarbs}g | 蛋 ${item.actualProtein}g | 脂 ${item.actualFat}g (${item.actualEnergy.toInt()} kcal)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        OutlinedButton(onClick = onDelete) {
            Text("删除", fontSize = 12.sp)
        }
    }
}
