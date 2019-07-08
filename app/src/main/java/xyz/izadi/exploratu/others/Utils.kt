package xyz.izadi.exploratu.others

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

}