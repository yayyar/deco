package com.yayyar.deco.feature.shift

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yayyar.deco.core.database.DecoDatabase
import com.yayyar.deco.core.database.entity.CashMovementEntity
import com.yayyar.deco.core.database.entity.ShiftEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ShiftViewModel(application: Application) : AndroidViewModel(application) {
    private val db = DecoDatabase.getInstance(application)
    private val shiftDao = db.shiftDao()

    val activeShift: StateFlow<ShiftEntity?> = shiftDao.getActiveShiftFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val cashMovements: StateFlow<List<CashMovementEntity>> = activeShift.flatMapLatest { shift ->
        if (shift != null) {
            shiftDao.getCashMovementsByShiftFlow(shift.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shiftHistory: StateFlow<List<ShiftEntity>> = shiftDao.getAllShiftsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openShift(openingFloat: Double, notes: String?) {
        viewModelScope.launch {
            val shift = ShiftEntity(
                id = UUID.randomUUID().toString(),
                openedAt = System.currentTimeMillis(),
                openingFloat = openingFloat,
                notes = notes,
                status = "OPEN"
            )
            shiftDao.insertShift(shift)
        }
    }

    fun recordCashMovement(type: String, amount: Double, reason: String) {
        val currentShift = activeShift.value ?: return
        viewModelScope.launch {
            val movement = CashMovementEntity(
                shiftId = currentShift.id,
                type = type,
                amount = amount,
                reason = reason
            )
            shiftDao.insertCashMovement(movement)
        }
    }

    fun closeShift(closingCashActual: Double, notes: String?) {
        val currentShift = activeShift.value ?: return
        viewModelScope.launch {
            val netAdjustments = shiftDao.getNetCashAdjustment(currentShift.id)
            val expectedCash = currentShift.openingFloat + currentShift.totalSalesCash + netAdjustments

            val closedShift = currentShift.copy(
                closedAt = System.currentTimeMillis(),
                closingCashActual = closingCashActual,
                closingCashExpected = expectedCash,
                notes = notes ?: currentShift.notes,
                status = "CLOSED"
            )
            shiftDao.updateShift(closedShift)
        }
    }
}
