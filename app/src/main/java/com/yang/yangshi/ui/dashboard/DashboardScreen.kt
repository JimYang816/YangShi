package com.yang.yangshi.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.LinearProgressIndicator
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
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToMealPortion: (MealType) -> Unit,
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
        // 1. 每日营养大盘核心进度 Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日碳蛋脂进度",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = state.selectedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // 热量
                MacroProgressBar(
                    label = "总热量",
                    consumed = "${state.totalConsumed.caloriesKcal.toInt()} / ${state.dailyTarget.caloriesKcal.toInt()} kcal",
                    progress = state.caloriesProgress,
                    remText = "剩余 ${state.remainingTarget.caloriesKcal.toInt()} kcal"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 碳水
                MacroProgressBar(
                    label = "碳水化合物",
                    consumed = "${state.totalConsumed.carbsGrams} / ${state.dailyTarget.carbsGrams} g",
                    progress = state.carbsProgress,
                    remText = "剩余 ${state.remainingTarget.carbsGrams} g"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 蛋白质
                MacroProgressBar(
                    label = "蛋白质",
                    consumed = "${state.totalConsumed.proteinGrams} / ${state.dailyTarget.proteinGrams} g",
                    progress = state.proteinProgress,
                    remText = "剩余 ${state.remainingTarget.proteinGrams} g"
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 脂肪
                MacroProgressBar(
                    label = "脂肪",
                    consumed = "${state.totalConsumed.fatGrams} / ${state.dailyTarget.fatGrams} g",
                    progress = state.fatProgress,
                    remText = "剩余 ${state.remainingTarget.fatGrams} g"
                )
            }
        }

        Text(
            text = "今日四餐打卡摘要",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // 2. 四餐打卡卡片 (早餐、午餐、晚餐、加餐)
        MealType.entries.forEach { mealType ->
            val mealLog = state.mealSummaries[mealType]
            MealSummaryCard(
                mealType = mealType,
                mealLog = mealLog,
                onAddClick = { onNavigateToMealPortion(mealType) }
            )
        }

        // 3. 折叠式历史打卡日志
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleHistoryExpanded() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "历史打卡日志",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isHistoryExpanded) "点击收起折叠栏" else "点击展开查看具体历史日志与细目",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Text(
                        text = if (state.isHistoryExpanded) "收起 ▲" else "展开 ▼",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(visible = state.isHistoryExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // 日期切换器
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // 历史细目列表
                        MealType.entries.forEach { mealType ->
                            val log = state.mealSummaries[mealType]
                            val isExpanded = state.expandedMealTypes.contains(mealType)

                            ExpandableMealLogSection(
                                mealType = mealType,
                                mealLog = log,
                                isExpanded = isExpanded,
                                onToggleExpand = { viewModel.toggleMealExpanded(mealType) },
                                onDeleteItem = viewModel::deleteLogItem
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    consumed: String,
    progress: Float,
    remText: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = "$consumed ($remText)", fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun MealSummaryCard(
    mealType: MealType,
    mealLog: MealLog?,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = mealType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(onClick = onAddClick) {
                    Text("打卡 / 配平")
                }
            }

            if (mealLog != null && mealLog.items.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "已摄入: 碳 ${mealLog.totalActualCarbs}g | 蛋 ${mealLog.totalActualProtein}g | 脂 ${mealLog.totalActualFat}g (${mealLog.totalActualEnergy.toInt()} kcal)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                mealLog.items.forEach { item ->
                    Text(
                        text = "• ${item.foodName} ${item.foodWeightG}g (${item.actualEnergy.toInt()} kcal)",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "暂无打卡记录，点击右侧按钮添加食物",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun ExpandableMealLogSection(
    mealType: MealType,
    mealLog: MealLog?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteItem: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${mealType.displayName} ${if (mealLog != null && mealLog.items.isNotEmpty()) "(${mealLog.items.size}项)" else "(无记录)"}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = if (isExpanded) "收起 ▲" else "展开 ▼",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (isExpanded && mealLog != null && mealLog.items.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            mealLog.items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "• ${item.foodName} ${item.foodWeightG}g",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "碳 ${item.actualCarbs}g | 蛋 ${item.actualProtein}g | 脂 ${item.actualFat}g (${item.actualEnergy.toInt()} kcal)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    OutlinedButton(onClick = { onDeleteItem(item.id) }) {
                        Text("删除", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
