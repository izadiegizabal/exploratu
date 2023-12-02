package xyz.izadi.exploratu


import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform


fun Activity.requestGDPRMessage() {
    val TAG = this::class.simpleName
    val params = ConsentRequestParameters.Builder()
        .setTagForUnderAgeOfConsent(false)
        .build()

    UserMessagingPlatform.getConsentInformation(this).apply {
        requestConsentInfoUpdate(
            this@requestGDPRMessage,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this@requestGDPRMessage
                ) {
                    if (it != null) {
                        // Consent gathering failed.
                        Log.w(TAG, "${it.errorCode}: ${it.message}")
                    }
                    // Consent has been gathered.
                }
            },
            {
                // Consent gathering failed.
                Log.w(TAG, "${it.errorCode}: ${it.message}")
            }
        )
    }
}