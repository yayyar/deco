package com.yayyar.deco.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yayyar.deco.core.database.entity.CashMovementEntity
import com.yayyar.deco.core.database.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Query("SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY opened_at DESC LIMIT 1")
    fun getActiveShiftFlow(): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts WHERE status = 'OPEN' ORDER BY opened_at DESC LIMIT 1")
    suspend fun getActiveShift(): ShiftEntity?

    @Query("SELECT * FROM shifts ORDER BY opened_at DESC")
    fun getAllShiftsFlow(): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: String): ShiftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashMovement(movement: CashMovementEntity): Long

    @Query("SELECT * FROM cash_movements WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    fun getCashMovementsByShiftFlow(shiftId: String): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM cash_movements WHERE shift_id = :shiftId ORDER BY timestamp DESC")
    suspend fun getCashMovementsByShift(shiftId: String): List<CashMovementEntity>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN type = 'CASH_IN' THEN amount ELSE 0.0 END), 0.0) -
            COALESCE(SUM(CASE WHEN type = 'CASH_OUT' THEN amount ELSE 0.0 END), 0.0)
        FROM cash_movements 
        WHERE shift_id = :shiftId
    """)
    suspend fun getNetCashAdjustment(shiftId: String): Double
}
