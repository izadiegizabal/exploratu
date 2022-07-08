package xyz.izadi.exploratu.currencies.data

import android.content.Context
import android.telephony.TelephonyManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.withContext
import xyz.izadi.exploratu.ApplicationScope
import xyz.izadi.exploratu.IoDispatcher
import xyz.izadi.exploratu.currencies.data.api.ExchangeRatesAPI
import xyz.izadi.exploratu.currencies.data.models.Currencies
import java.util.*
import javax.inject.Inject

class RatesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
    private val ratesDatabase: RatesDatabase,
    private val ratesAPI: ExchangeRatesAPI
) {
    fun getCurrencies(): Flow<Currencies?> = channelFlow {
        withContext(ioDispatcher) {
            runCatching {
                context.assets
                    .open("currencyInfo.json")
                    .bufferedReader()
                    .use { it.readText() }
                    .run {
                        Gson().fromJson(this, Currencies::class.java)
                    }
            }.getOrNull()?.let {
                send(it)
            }
        }
    }.also {
        it.launchIn(scope)
    }

    suspend fun syncRates() = withContext(ioDispatcher) {
        val currentRates = getRates().firstOrNull()
        if (currentRates?.haveBeenRefreshedToday() != true) {
            runCatching { ratesAPI.getLatestRates() }.getOrNull()?.let {
                ratesDatabase.ratesDao().insertRates(it)
            }
        }
    }

    fun getRates() = ratesDatabase.ratesDao().getLatestRates()

    fun getDetectedCurrency(): String? =
        (context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager)
            ?.networkCountryIso?.takeIf { it.isNotBlank() }?.run {
                getCurrencyCodeFromCountryISO(this)
            }

    private fun getCurrencyCodeFromCountryISO(countryISO: String): String? = runCatching {
        getCurrencyCodeFromLocale(Locale("", countryISO))
    }.getOrNull()

    fun getCurrencyCodeFromDeviceLocale(): String? =
        getCurrencyCodeFromLocale(Locale.getDefault())

    private fun getCurrencyCodeFromLocale(locale: Locale): String? {
        try {
            return Currency.getInstance(locale).currencyCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
