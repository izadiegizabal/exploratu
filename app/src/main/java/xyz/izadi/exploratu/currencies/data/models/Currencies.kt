package xyz.izadi.exploratu.currencies.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Parcelize
@Serializable
data class Currencies(
    @SerialName("version")
    val version: Float,
    @Serializable(with = DateSerializer::class)
    @SerialName("versionDate")
    val versionDate: Date,
    @SerialName("currencies")
    val currencies: List<Currency>
) : Parcelable {
    fun getCurrency(currencyCodeToSearch: String): Currency? = currencies.find {
        it.code == currencyCodeToSearch
    }
}
