package xyz.izadi.exploratu.currencies.data

import androidx.room.*
import xyz.izadi.exploratu.currencies.data.models.Rates

@Dao
interface RatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: Rates)

    @Update
    suspend fun updateRates(rates: Rates)

    @Delete
    suspend fun deleteRates(rates: Rates)

    @Query("SELECT * FROM Rates ORDER BY date DESC LIMIT 1")
    suspend fun getLatestRates(): Rates
}