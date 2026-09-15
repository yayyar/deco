package com.yayyar.teahouse.feature.pos

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.teahouse.core.common.Formatters
import com.yayyar.teahouse.core.printer.ReceiptData
import com.yayyar.teahouse.core.ui.components.CurrencyText
import com.yayyar.teahouse.ui.theme.AccentGreen

import androidx.compose.ui.res.stringResource
import com.yayyar.teahouse.R

@Composable
fun ReceiptSuccessDialog(
    receiptData: ReceiptData,
    onDismiss: () -> Unit,
    onPrint: (ReceiptData) -> Unit,
    onShareSlip: (ReceiptData) -> Unit
) {
    var previewAction by remember { mutableStateOf<ReceiptPreviewAction?>(null) }

    if (previewAction != null) {
        ReceiptPreviewDialog(
            receiptData = receiptData,
            initialAction = previewAction ?: ReceiptPreviewAction.PRINT,
            onDismiss = { previewAction = null },
            onPrint = { onPrint(it) },
            onShare = { onShareSlip(it) }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(AccentGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.receipt_success_title),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "${stringResource(R.string.receipt_number_label)}: ${receiptData.receiptNumber}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
//                        Text("${stringResource(R.string.pos_items_count, receiptData.items.size)}:")
                        Text("${receiptData.items.sumOf { it.quantity }} pcs", fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.checkout_grand_total_label), fontWeight = FontWeight.Bold)
                        CurrencyText(
                            amount = receiptData.grandTotal,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.checkout_payment_method), fontSize = 13.sp)
                        Text(receiptData.paymentType, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    if (receiptData.changeReturned > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.checkout_change_returned), fontSize = 13.sp)
                            CurrencyText(amount = receiptData.changeReturned, fontSize = 13.sp, color = AccentGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { previewAction = ReceiptPreviewAction.SHARE },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_share), fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { previewAction = ReceiptPreviewAction.PRINT },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.action_print), fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.receipt_new_sale_btn))
            }
        }
    )
}
