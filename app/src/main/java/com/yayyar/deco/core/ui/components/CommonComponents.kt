package com.yayyar.deco.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.ui.theme.AccentGreen
import com.yayyar.deco.ui.theme.AccentRed

@Composable
fun CurrencyText(
    amount: Double,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp
) {
    Text(
        text = Formatters.formatMmk(amount),
        modifier = modifier,
        color = color,
        fontWeight = fontWeight,
        fontSize = fontSize
    )
}

@Composable
fun StockBadge(
    stockQty: Int,
    lowStockThreshold: Int = 5,
    modifier: Modifier = Modifier
) {
    val isLowStock = stockQty <= lowStockThreshold
    val isOutOfStock = stockQty <= 0

    val backgroundColor = when {
        isOutOfStock -> AccentRed
        isLowStock -> Color(0xFFF97316) // Orange
        else -> AccentGreen.copy(alpha = 0.85f)
    }

    val label = when {
        isOutOfStock -> "Out of Stock"
        isLowStock -> "Low: $stockQty left"
        else -> "$stockQty in stock"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PaymentMethodBadge(
    paymentType: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (paymentType.uppercase()) {
        "CASH" -> Color(0xFF10B981).copy(alpha = 0.15f) to Color(0xFF059669)
        "KPAY" -> Color(0xFF2563EB).copy(alpha = 0.15f) to Color(0xFF1D4ED8)
        "WAVEPAY" -> Color(0xFF8B5CF6).copy(alpha = 0.15f) to Color(0xFF7C3AED)
        else -> Color(0xFF64748B).copy(alpha = 0.15f) to Color(0xFF475569)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = paymentType,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
