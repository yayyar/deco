package com.yayyar.deco.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.data.repository.PaymentMethodRepository
import com.yayyar.deco.core.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean?> = preferencesRepository.isDarkMode
    val selectedLanguage: StateFlow<String> = preferencesRepository.selectedLanguage
    val selectedPrinterName: StateFlow<String?> = preferencesRepository.selectedPrinterName

    val activePaymentMethodsCount: StateFlow<Int> = paymentMethodRepository.getActivePaymentMethodsFlow()
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun setDarkMode(enabled: Boolean?) {
        preferencesRepository.setDarkMode(enabled)
    }

    fun setSelectedLanguage(lang: String) {
        preferencesRepository.setSelectedLanguage(lang)
    }
}
