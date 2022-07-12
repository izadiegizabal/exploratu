package xyz.izadi.exploratu.currencies

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import xyz.izadi.exploratu.BuildConfig
import xyz.izadi.exploratu.currencies.data.RatesRepository
import javax.inject.Inject

@HiltViewModel
class CurrenciesViewModel @Inject constructor(
    private val app: Application,
    private val ratesRepository: RatesRepository
) : ViewModel() {

    private var adRequest = AdRequest.Builder().build()
    private var interstitialAd: InterstitialAd? = null
    private val adCallback: (() -> Unit) -> FullScreenContentCallback = { onSuccess ->
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                interstitialAd = null
                onSuccess()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                interstitialAd = null
                onSuccess()
            }
        }
    }

    val detectedCurrency
        get() = ratesRepository.getDetectedCurrency()
    val localeCurrency
        get() = ratesRepository.getCurrencyCodeFromDeviceLocale()

    val currencies = ratesRepository.getCurrencies()

    val rates = ratesRepository.getRates()

    fun syncRates() = viewModelScope.launch {
        ratesRepository.syncRates()
    }

    fun loadAd() {
        if (interstitialAd == null) {
            InterstitialAd.load(
                app,
                BuildConfig.ADS_INTERSTITIAL,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.d(TAG, adError.toString())
                        interstitialAd = null
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Ad was loaded")
                        interstitialAd = ad
                    }
                }
            )
        }
    }

    fun showAd(over: Activity, afterAd: () -> Unit) {
        interstitialAd?.apply {
            fullScreenContentCallback = adCallback(afterAd)
            show(over)
        } ?: run {
            afterAd()
        }
    }

    companion object {
        const val TAG = "CurrenciesViewModel"
    }
}
