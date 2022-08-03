package xyz.izadi.exploratu.currencies.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Currency(
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("sign")
    val sign: String,
    @SerializedName("defaultValue")
    val defaultValue: Float,
    @SerializedName("countries")
    val countries: List<String>
) : Parcelable
