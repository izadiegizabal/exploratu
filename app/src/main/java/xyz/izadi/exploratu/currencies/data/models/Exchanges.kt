package xyz.izadi.exploratu.currencies.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Exchanges(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val AUD: Float,
    val BGN: Float,
    val BRL: Float,
    val CAD: Float,
    val CHF: Float,
    val CNY: Float,
    val CZK: Float,
    val DKK: Float,
    var EUR: Float,
    val GBP: Float,
    val HKD: Float,
    val HRK: Float,
    val HUF: Float,
    val IDR: Float,
    val ILS: Float,
    val INR: Float,
    val ISK: Float,
    val JPY: Float,
    val KRW: Float,
    val MXN: Float,
    val MYR: Float,
    val NOK: Float,
    val NZD: Float,
    val PHP: Float,
    val PLN: Float,
    val RON: Float,
    val RUB: Float,
    val SEK: Float,
    val SGD: Float,
    val THB: Float,
    val TRY: Float,
    val USD: Float,
    val ZAR: Float
) {
    fun getRate(from: String): Float? {
        val field = Exchanges::class.java.getDeclaredField(from)
        field.isAccessible
        return field.getFloat(this)
    }

    init {
        EUR = 1.0f
    }

    fun resetEur(){
        EUR = 1.0f
    }

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