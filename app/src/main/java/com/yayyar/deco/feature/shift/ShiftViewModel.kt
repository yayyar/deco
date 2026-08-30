package com.yayyar.deco.feature.shift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.common.Resource
import com.yayyar.deco.core.data.repository.ShiftRepository
import com.yayyar.deco.core.database.entity.CashMovementEntity
import com.yayyar.deco.core.database.entity.ShiftEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository
) : ViewModel() {

    val activeShift: StateFlow<ShiftEntity?> = shiftRepository.getActiveShiftFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val cashMovements: StateFlow<List<CashMovementEntity>> = activeShift.flatMapLatest { shift ->
        if (shift != null) {
            shiftRepository.getCashMovementsByShiftFlow(shift.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shiftHistory: StateFlow<List<ShiftEntity>> = shiftRepository.getAllShiftsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _shiftActionState = MutableStateFlow<Resource<ShiftEntity>?>(null)
    val shiftActionState: StateFlow<Resource<ShiftEntity>?> = _shiftActionState.asStateFlow()

    fun openShift(openingFloat: Double, notes: String?) {
        viewModelScope.launch {
            _shiftActionState.value = Resource.Loading
            val result = shiftRepository.openShift(openingFloat, notes)
            _shiftActionState.value = result
        }
    }

    fun recordCashMovement(type: String, amount: Double, reason: String) {
        val shiftId = activeShift.value?.id ?: return
        viewModelScope.launch {
            shiftRepository.addCashMovement(
                shiftId = shiftId,
                amount = amount,
                reason = reason,
                type = type
            )
        }
    }

    fun closeShift(closingCashActual: Double, notes: String?) {
        val shiftId = activeShift.value?.id ?: return
        viewModelScope.launch {
            _shiftActionState.value = Resource.Loading
            val result = shiftRepository.closeShift(shiftId, closingCashActual, notes)
            _shiftActionState.value = result
        }
    }

    fun clearActionState() {
        _shiftActionState.value = null
    }
}
