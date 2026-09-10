package com.yayyar.deco.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.data.repository.PaymentMethodRepository
import com.yayyar.deco.core.database.entity.PaymentMethodEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentMethodViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    val paymentMethods: StateFlow<List<PaymentMethodEntity>> = paymentMethodRepository
        .getAllPaymentMethodsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleActiveStatus(paymentMethod: PaymentMethodEntity, isActive: Boolean) {
        viewModelScope.launch {
            paymentMethodRepository.updateActiveStatus(paymentMethod.id, isActive)
        }
    }

    fun savePaymentMethod(
        id: String?,
        name: String,
        code: String,
        accountName: String?,
        accountNumber: String?,
        qrCodeData: String?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            val entity = PaymentMethodEntity(
                id = id ?: UUID.randomUUID().toString(),
                name = name.trim(),
                code = code.trim().uppercase(),
                accountName = accountName?.trim()?.ifBlank { null },
                accountNumber = accountNumber?.trim()?.ifBlank { null },
                qrCodeData = qrCodeData?.trim()?.ifBlank { null },
                isActive = isActive,
                sortOrder = if (id != null) {
                    paymentMethods.value.find { it.id == id }?.sortOrder ?: (paymentMethods.value.size + 1)
                } else {
                    paymentMethods.value.size + 1
                }
            )
            paymentMethodRepository.insertPaymentMethod(entity)
        }
    }

    fun deletePaymentMethod(paymentMethod: PaymentMethodEntity) {
        viewModelScope.launch {
            paymentMethodRepository.deletePaymentMethod(paymentMethod)
        }
    }
}
