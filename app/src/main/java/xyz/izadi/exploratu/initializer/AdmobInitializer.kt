package xyz.izadi.exploratu.initializer

import android.content.Context
import androidx.startup.Initializer
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.qualifiers.ApplicationContext
import xyz.izadi.exploratu.di.getEntryPoint
import javax.inject.Inject

class AdmobInitializer : Initializer<Unit> {
    @[Inject ApplicationContext]
    lateinit var context: Context

    override fun create(context: Context) {
        context.getEntryPoint().inject(this)
        MobileAds.initialize(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
