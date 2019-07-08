package xyz.izadi.exploratu.currencies.models

data class Exchanges(
    val EUR: Float,
    val CAD: Float,
    val HKD: Float,
    val ISK: Float,
    val PHP: Float,
    val DKK: Float,
    val HUF: Float,
    val CZK: Float,
    val AUD: Float,
    val RON: Float,
    val SEK: Float,
    val IDR: Float,
    val INR: Float,
    val BRL: Float,
    val RUB: Float,
    val HRK: Float,
    val JPY: Float,
    val THB: Float,
    val CHF: Float,
    val SGD: Float,
    val PLN: Float,
    val BGN: Float,
    val TRY: Float,
    val CNY: Float,
    val NOK: Float,
    val NZD: Float,
    val ZAR: Float,
    val USD: Float,
    val MXN: Float,
    val ILS: Float,
    val GBP: Float,
    val KRW: Float,
    val MYR: Float
) {
    fun getRate(from: String): Float? {
        val field = Exchanges::class.java.getDeclaredField(from)
        field.isAccessible
        return field.getFloat(this)
    }

    constructor(rates: ArrayList<Float>) : this(
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