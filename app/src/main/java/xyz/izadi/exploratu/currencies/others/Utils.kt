package xyz.izadi.exploratu.currencies.others

import java.math.BigDecimal

object Utils {

    fun insertPeriodically(text: String, insert: String, period: Int): String {
        val builder = StringBuilder(text)

        var idx = builder.length - period

        while (idx > 0) {
            builder.insert(idx, insert)
            idx -= period
        }

        return builder.toString()
    }

    /**
     * Round to certain number of decimals
     *
     * @param numberToRound
     * @param decimalPlaces
     * @return
     */
    fun round(numberToRound: Float, decimalPlaces: Int): Float {
        var bd = BigDecimal(numberToRound.toString())
        bd = bd.setScale(decimalPlaces, BigDecimal.ROUND_HALF_UP)
        return bd.toFloat()
    }

}