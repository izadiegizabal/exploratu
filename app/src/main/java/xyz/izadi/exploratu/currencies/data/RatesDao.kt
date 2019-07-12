package xyz.izadi.exploratu.currencies.data

import androidx.room.*
import xyz.izadi.exploratu.currencies.data.models.Rates

@Dao
interface RatesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRates(rates: Rates)

    @Update
    fun updateRates(rates: Rates)

    @Delete
    fun deleteRates(rates: Rates)

    @Query("SELECT * FROM Rates ORDER BY timestamp DESC LIMIT 1")
    fun getLatestRates(): Rates
}