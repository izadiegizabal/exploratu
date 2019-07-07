package xyz.izadi.exploratu.currencies.models

import java.util.*

data class Currencies (
    val version: Float,
    val versionDate: Date,
    val totalCurrencies: Int,
    val currencies: Array<Currency>
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Currencies

        if (version != other.version) return false
        if (versionDate != other.versionDate) return false
        if (totalCurrencies != other.totalCurrencies) return false
        if (!currencies.contentEquals(other.currencies)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + versionDate.hashCode()
        result = 31 * result + totalCurrencies
        result = 31 * result + currencies.contentHashCode()
        return result
    }
}