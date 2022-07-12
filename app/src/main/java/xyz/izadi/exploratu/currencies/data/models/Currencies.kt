package xyz.izadi.exploratu.currencies.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Currencies(
    @SerializedName("version")
    val version: Float,
    @SerializedName("versionDate")
    val versionDate: Date,
    @SerializedName("currencies")
    val currencies: List<Currency>
) : Parcelable {
    fun getCurrency(currencyCodeToSearch: String): Currency? = currencies.find {
        it.code == currencyCodeToSearch
    }
}
