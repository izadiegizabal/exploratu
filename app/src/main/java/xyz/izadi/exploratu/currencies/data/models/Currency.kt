package xyz.izadi.exploratu.currencies.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Currency(
    @SerialName("name")
    val name: String,
    @SerialName("code")
    val code: String,
    @SerialName("sign")
    val sign: String,
    @SerialName("defaultValue")
    val defaultValue: Float,
    @SerialName("countries")
    val countries: List<String>
) : Parcelable
