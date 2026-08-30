package com.yayyar.deco.core.data.repository

import com.yayyar.deco.core.common.Resource
import com.yayyar.deco.core.database.dao.ShiftDao
import com.yayyar.deco.core.database.entity.CashMovementEntity
import com.yayyar.deco.core.database.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface ShiftRepository {
    fun getActiveShiftFlow(): Flow<ShiftEntity?>
    suspend fun getActiveShift(): ShiftEntity?
    fun getAllShiftsFlow(): Flow<List<ShiftEntity>>
    suspend fun getShiftById(id: String): ShiftEntity?
    fun getCashMovementsByShiftFlow(shiftId: String): Flow<List<CashMovementEntity>>
    suspend fun getCashMovementsByShift(shiftId: String): List<CashMovementEntity>
    suspend fun getNetCashAdjustment(shiftId: String): Double
    suspend fun openShift(openingFloat: Double, notes: String?): Resource<ShiftEntity>
    suspend fun addCashMovement(
        shiftId: String,
        amount: Double,
        reason: String,
        type: String
    ): Resource<CashMovementEntity>
    suspend fun closeShift(
        shiftId: String,
        closingCashActual: Double,
        notes: String?
    ): Resource<ShiftEntity>
    suspend fun updateShift(shift: ShiftEntity)
}

@Singleton
class ShiftRepositoryImpl @Inject constructor(
    private val shiftDao: ShiftDao
) : ShiftRepository {

    override fun getActiveShiftFlow(): Flow<ShiftEntity?> =
        shiftDao.getActiveShiftFlow()

    override suspend fun getActiveShift(): ShiftEntity? =
        shiftDao.getActiveShift()

    override fun getAllShiftsFlow(): Flow<List<ShiftEntity>> =
        shiftDao.getAllShiftsFlow()

    override suspend fun getShiftById(id: String): ShiftEntity? =
        shiftDao.getShiftById(id)

    override fun getCashMovementsByShiftFlow(shiftId: String): Flow<List<CashMovementEntity>> =
        shiftDao.getCashMovementsByShiftFlow(shiftId)

    override suspend fun getCashMovementsByShift(shiftId: String): List<CashMovementEntity> =
        shiftDao.getCashMovementsByShift(shiftId)

    override suspend fun getNetCashAdjustment(shiftId: String): Double =
        shiftDao.getNetCashAdjustment(shiftId)

    override suspend fun openShift(openingFloat: Double, notes: String?): Resource<ShiftEntity> {
        return try {
            val active = shiftDao.getActiveShift()
            if (active != null) {
                return Resource.Error("A shift is already open.")
            }
            val shift = ShiftEntity(
                id = UUID.randomUUID().toString(),
                openedAt = System.currentTimeMillis(),
                openingFloat = openingFloat,
                notes = notes,
                status = "OPEN"
            )
            shiftDao.insertShift(shift)
            Resource.Success(shift)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to open shift")
        }
    }

    override suspend fun addCashMovement(
        shiftId: String,
        amount: Double,
        reason: String,
        type: String
    ): Resource<CashMovementEntity> {
        return try {
            val movement = CashMovementEntity(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                type = type,
                amount = amount,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
            shiftDao.insertCashMovement(movement)
            Resource.Success(movement)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to record cash movement")
        }
    }

    override suspend fun closeShift(
        shiftId: String,
        closingCashActual: Double,
        notes: String?
    ): Resource<ShiftEntity> {
        return try {
            val shift = shiftDao.getShiftById(shiftId)
                ?: return Resource.Error("Shift not found")

            val netCashAdjustment = shiftDao.getNetCashAdjustment(shiftId)
            val expectedCash = shift.openingFloat + shift.totalSalesCash + netCashAdjustment

            val closedShift = shift.copy(
                closedAt = System.currentTimeMillis(),
                closingCashActual = closingCashActual,
                closingCashExpected = expectedCash,
                status = "CLOSED",
                notes = notes ?: shift.notes
            )
            shiftDao.updateShift(closedShift)
            Resource.Success(closedShift)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to close shift")
        }
    }

    override suspend fun updateShift(shift: ShiftEntity) {
        shiftDao.updateShift(shift)
    }
}
