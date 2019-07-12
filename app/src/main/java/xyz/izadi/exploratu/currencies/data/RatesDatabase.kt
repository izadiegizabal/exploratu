package xyz.izadi.exploratu.currencies.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.izadi.exploratu.currencies.data.models.Exchanges
import xyz.izadi.exploratu.currencies.data.models.Rates

@Database(
    entities = [Rates::class, Exchanges::class],
    version = 1
)
@TypeConverters(DateTypeConverter::class)
abstract class RatesDatabase : RoomDatabase() {
    abstract fun ratesDao(): RatesDao
    companion object {
        private var INSTANCE: RatesDatabase? = null

        fun getInstance(context: Context): RatesDatabase? {
            if (INSTANCE == null) {
                synchronized(RatesDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        RatesDatabase::class.java, "rates.db")
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}