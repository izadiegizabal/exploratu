package xyz.izadi.exploratu.currencies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import xyz.izadi.exploratu.currencies.data.RatesRepository
import javax.inject.Inject

@HiltViewModel
class CurrenciesViewModel @Inject constructor(
    private val ratesRepository: RatesRepository
) : ViewModel() {

    val detectedCurrency
        get() = ratesRepository.getDetectedCurrency()
    val localeCurrency
        get() = ratesRepository.getCurrencyCodeFromDeviceLocale()

    val currencies = ratesRepository.getCurrencies()

    val rates = ratesRepository.getRates()

    init {
        syncRates()
    }

    fun syncRates() = viewModelScope.launch {
        ratesRepository.syncRates()
    }
}
