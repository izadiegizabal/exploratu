package xyz.izadi.exploratu.currencies

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import xyz.izadi.exploratu.BuildConfig
import xyz.izadi.exploratu.currencies.data.RatesRepository
import javax.inject.Inject


@HiltViewModel
class CurrenciesViewModel @Inject constructor(
    private val ratesRepository: RatesRepository
) : ViewModel() {
    private var loadedAd: String? = null
    private val adLoadListener = object : IUnityAdsLoadListener {
        override fun onUnityAdsAdLoaded(placementId: String) {
            Log.d(TAG, "Unity Ads loaded ad for $placementId")
            loadedAd = placementId
        }

        override fun onUnityAdsFailedToLoad(
            placementId: String,
            error: UnityAds.UnityAdsLoadError,
            message: String
        ) {
            loadedAd = null
            Log.e(
                TAG,
                "Unity Ads failed to load ad for $placementId with error: [$error] $message"
            )
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
        if (loadedAd == null && UnityAds.isInitialized && UnityAds.isSupported) {
            UnityAds.load(BuildConfig.ADS_INTERSTITIAL, adLoadListener)
        }
    }

    fun showAd(over: Activity, afterAd: () -> Unit) {
        fun completed() {
            loadedAd = null
            afterAd()
        }

        loadedAd?.apply {
            UnityAds.show(
                activity = over,
                placementId = loadedAd,
                showListener = object : IUnityAdsShowListener {
                    override fun onUnityAdsShowFailure(
                        placementId: String?,
                        error: UnityAds.UnityAdsShowError?,
                        message: String?
                    ) {
                        Log.e(
                            TAG,
                            "Unity Ads failed to show ad for $placementId with error: [$error] $message"
                        )
                        completed()
                    }

                    override fun onUnityAdsShowStart(placementId: String?) {
                        /* no-op */
                    }

                    override fun onUnityAdsShowClick(placementId: String?) {
                        /* no-op */
                    }

                    override fun onUnityAdsShowComplete(
                        placementId: String?,
                        state: UnityAds.UnityAdsShowCompletionState?
                    ) {
                        completed()
                    }

                },
            )
        } ?: run {
            completed()
        }
    }

    companion object {
        val TAG: String = this::class.java.simpleName
    }
}
