package xyz.izadi.exploratu.currencies.data

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.currencies.data.models.toRates
import xyz.izadi.exploratu.di.ApplicationScope
import javax.inject.Inject

class PrepopulateRatesCallback @Inject constructor(
    private val ratesDao: Lazy<RatesDao>,
    @ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        scope.launch(Dispatchers.IO) {
            runCatching {
                val jsonString = context.assets
                    .open("currencyInfo.json")
                    .bufferedReader().use {
                        it.readText()
                    }
                Json.decodeFromString<Currencies>(jsonString).toRates()
            }.getOrNull()?.let {
                ratesDao.get().insertRates(it)
            }
        }
    }
}
