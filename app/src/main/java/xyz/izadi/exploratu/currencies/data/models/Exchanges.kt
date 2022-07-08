package xyz.izadi.exploratu.currencies.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Exchanges(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @SerializedName("aud") val AUD: Float,
    @SerializedName("bgn") val BGN: Float,
    @SerializedName("brl") val BRL: Float,
    @SerializedName("cad") val CAD: Float,
    @SerializedName("chf") val CHF: Float,
    @SerializedName("cny") val CNY: Float,
    @SerializedName("czk") val CZK: Float,
    @SerializedName("dkk") val DKK: Float,
    @SerializedName("eur") val EUR: Float,
    @SerializedName("gbp") val GBP: Float,
    @SerializedName("hkd") val HKD: Float,
    @SerializedName("hrk") val HRK: Float,
    @SerializedName("huf") val HUF: Float,
    @SerializedName("idr") val IDR: Float,
    @SerializedName("ils") val ILS: Float,
    @SerializedName("inr") val INR: Float,
    @SerializedName("isk") val ISK: Float,
    @SerializedName("jpy") val JPY: Float,
    @SerializedName("krw") val KRW: Float,
    @SerializedName("mxn") val MXN: Float,
    @SerializedName("myr") val MYR: Float,
    @SerializedName("nok") val NOK: Float,
    @SerializedName("nzd") val NZD: Float,
    @SerializedName("php") val PHP: Float,
    @SerializedName("pln") val PLN: Float,
    @SerializedName("ron") val RON: Float,
    @SerializedName("rub") val RUB: Float,
    @SerializedName("sek") val SEK: Float,
    @SerializedName("sgd") val SGD: Float,
    @SerializedName("thb") val THB: Float,
    @SerializedName("try") val TRY: Float,
    @SerializedName("usd") val USD: Float,
    @SerializedName("zar") val ZAR: Float
) {
    fun getRate(from: String): Float? = runCatching {
        Exchanges::class.java.getDeclaredField(from).getFloat(this)
    }.getOrNull()

    constructor(rates: ArrayList<Float>) : this(
        null,
        rates[0],
        rates[1],
        rates[2],
        rates[3],
        rates[4],
        rates[5],
        rates[6],
        rates[7],
        rates[8],
        rates[9],
        rates[10],
        rates[11],
        rates[12],
        rates[13],
        rates[14],
        rates[15],
        rates[16],
        rates[17],
        rates[18],
        rates[19],
        rates[20],
        rates[21],
        rates[22],
        rates[23],
        rates[24],
        rates[25],
        rates[26],
        rates[27],
        rates[28],
        rates[29],
        rates[30],
        rates[31],
        rates[32]
    )
}
