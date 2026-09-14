package com.yayyar.deco.feature.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Drafts
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yayyar.deco.core.common.Formatters
import com.yayyar.deco.core.database.model.OrderWithItems
import com.yayyar.deco.core.ui.components.CurrencyText
import com.yayyar.deco.ui.theme.AccentGold
import com.yayyar.deco.ui.theme.AccentRed
import com.yayyar.deco.ui.theme.PrimaryLight

import androidx.compose.ui.res.stringResource
import com.yayyar.deco.R

@Composable
fun DraftSalesDialog(
    draftOrders: List<OrderWithItems>,
    onDismiss: () -> Unit,
    onRestoreDraft: (OrderWithItems) -> Unit,
    onDeleteDraft: (String) -> Unit
) {
    var deletingDraftId by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.drafts_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        },
        text = {
            if (draftOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = stringResource(R.string.drafts_empty),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.drafts_empty_sub),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(draftOrders, key = { it.order.id }) { draft ->
                        DraftSaleCard(
                            draftOrder = draft,
                            onResume = {
                                onRestoreDraft(draft)
                                onDismiss()
                            },
                            onDelete = { deletingDraftId = draft.order.id }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )

    deletingDraftId?.let { draftId ->
        AlertDialog(
            onDismissRequest = { deletingDraftId = null },
            title = { Text(stringResource(R.string.drafts_delete)) },
            text = { Text(stringResource(R.string.drafts_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDraft(draftId)
                        deletingDraftId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingDraftId = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun DraftSaleCard(
    draftOrder: OrderWithItems,
    onResume: () -> Unit,
    onDelete: () -> Unit
) {
    val totalQty = draftOrder.items.sumOf { it.quantity }
    val timeFormatted = Formatters.formatDateTime(draftOrder.order.createdAt)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onResume)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Surface(
//                        color = AccentGold.copy(alpha = 0.15f),
//                        shape = RoundedCornerShape(4.dp)
//                    ) {
//                        Text(
//                            text = draftOrder.order.receiptNumber,
//                            fontSize = 11.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = AccentGold,
//                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
//                        )
//                    }
//                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                CurrencyText(
                    amount = draftOrder.order.grandTotal,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (!draftOrder.order.customerName.isNullOrBlank()) {
                Text(
                    text = "Customer: ${draftOrder.order.customerName} (${draftOrder.order.customerPhone ?: ""})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Items summary
            Text(
                text = "${totalQty} item(s) • ${draftOrder.items.joinToString(", ") { "${it.productName} (x${it.quantity})" }}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Draft",
                        tint = AccentRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

//                Spacer(Modifier.width(8.dp))

//                OutlinedButton(
//                    onClick = onResume,
//                    shape = RoundedCornerShape(8.dp),
//                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
//                    modifier = Modifier.height(32.dp)
//                ) {
//                    Icon(
//                        imageVector = Icons.Outlined.PlayArrow,
//                        contentDescription = null,
//                        modifier = Modifier.size(16.dp)
//                    )
//                    Spacer(Modifier.width(4.dp))
//                    Text("Resume Sale", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
//                }
            }
        }
    }
}
