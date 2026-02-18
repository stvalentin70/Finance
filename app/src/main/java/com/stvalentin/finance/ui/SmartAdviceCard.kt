package com.stvalentin.finance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stvalentin.finance.data.UserProfile

enum class AdvicePriority {
    CRITICAL,    // 🔴 Красный - срочно требуется действие
    HIGH,        // 🟠 Оранжевый - важные предупреждения
    MEDIUM,      // 🟡 Желтый - обратить внимание
    NORMAL,      // 🟢 Зеленый - всё хорошо
    ACHIEVEMENT  // 🔵 Синий - достижения, позитивные советы
}

data class SmartAdvice(
    val id: Int,
    val title: String,
    val description: String,
    val priority: AdvicePriority,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val action: String? = null,
    val route: String? = null,
    val actionIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.ChevronRight
)

@Composable
fun SmartAdviceCard(
    advice: SmartAdvice,
    profile: UserProfile?,
    onClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, borderColor, iconColor) = when (advice.priority) {
        AdvicePriority.CRITICAL -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.error
        )
        AdvicePriority.HIGH -> Triple(
            Color(0xFFFFF3E0), // Светло-оранжевый
            Color(0xFFFF9800),
            Color(0xFFFF9800)
        )
        AdvicePriority.MEDIUM -> Triple(
            Color(0xFFFFF9C4), // Светло-желтый
            Color(0xFFFFC107),
            Color(0xFFFFC107)
        )
        AdvicePriority.NORMAL -> Triple(
            Color(0xFFE8F5E9), // Светло-зеленый
            Color(0xFF4CAF50),
            Color(0xFF4CAF50)
        )
        AdvicePriority.ACHIEVEMENT -> Triple(
            Color(0xFFE3F2FD), // Светло-синий
            Color(0xFF2196F3),
            Color(0xFF2196F3)
        )
    }
    
    val priorityEmoji = when (advice.priority) {
        AdvicePriority.CRITICAL -> "🔴"
        AdvicePriority.HIGH -> "🟠"
        AdvicePriority.MEDIUM -> "🟡"
        AdvicePriority.NORMAL -> "🟢"
        AdvicePriority.ACHIEVEMENT -> "🔵"
    }
    
    val statusEmojis = profile?.getActiveStatusEmojis() ?: ""
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(advice.route) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Верхняя строка с приоритетом и статусами
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Приоритет и эмодзи
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(borderColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = priorityEmoji,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = when (advice.priority) {
                            AdvicePriority.CRITICAL -> "Срочно!"
                            AdvicePriority.HIGH -> "Важно"
                            AdvicePriority.MEDIUM -> "Обратите внимание"
                            AdvicePriority.NORMAL -> "Хорошо"
                            AdvicePriority.ACHIEVEMENT -> "Достижение"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = borderColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Статусы пользователя
                if (statusEmojis.isNotEmpty()) {
                    Text(
                        text = statusEmojis,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Основной контент совета
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Иконка
                Icon(
                    imageVector = advice.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Текст совета
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (advice.title.isNotEmpty()) {
                        Text(
                            text = advice.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Text(
                        text = advice.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                
                // Кнопка действия
                if (advice.route != null) {
                    Icon(
                        imageVector = advice.actionIcon,
                        contentDescription = advice.action ?: "Перейти",
                        tint = borderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Дополнительная информация (если есть)
            if (advice.action != null && advice.route == null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = borderColor.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = advice.action,
                        style = MaterialTheme.typography.bodySmall,
                        color = borderColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}