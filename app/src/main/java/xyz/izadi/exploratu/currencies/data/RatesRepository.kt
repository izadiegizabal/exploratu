package xyz.izadi.exploratu.currencies.data

import android.content.Context
import android.telephony.TelephonyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import xyz.izadi.exploratu.currencies.data.api.ExchangeRatesAPI
import xyz.izadi.exploratu.currencies.data.models.Currencies
import xyz.izadi.exploratu.di.ApplicationScope
import xyz.izadi.exploratu.di.IoDispatcher
import java.util.Currency
import java.util.Locale
import javax.inject.Inject

class RatesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ApplicationScope private val scope: CoroutineScope,
    private val ratesDatabase: RatesDatabase,
    private val ratesAPI: ExchangeRatesAPI
) {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun getCurrencies(): Flow<Currencies?> = channelFlow {
        withContext(ioDispatcher) {
            runCatching {
                context.assets
                    .open("currencyInfo.json")
                    .bufferedReader()
                    .use { it.readText() }
                    .run {
                        jsonParser.decodeFromString<Currencies>(this)
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

    fun getCurrencyCodeFromDeviceLocale(): String? = getCurrencyCodeFromLocale(
        context.resources.configuration.locales.get(0)
    )

    private fun getCurrencyCodeFromLocale(locale: Locale): String? {
        try {
            return Currency.getInstance(locale).currencyCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
