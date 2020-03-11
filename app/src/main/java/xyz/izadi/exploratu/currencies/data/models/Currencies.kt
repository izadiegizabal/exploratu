package xyz.izadi.exploratu.currencies.data.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*
import kotlin.collections.ArrayList


data class Currencies(
    val version: Float,
    val versionDate: Date,
    val totalCurrencies: Int,
    val currencies: Array<Currency>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        Date(parcel.readLong()),
        parcel.readInt(),
        parcel.createTypedArray(Currency)!!
    )

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

    fun getCurrency(currencyCodeToSearch: String): Currency? {
        for (currency: Currency in currencies) {
            if (currency.code == currencyCodeToSearch) {
                return currency
            }
        }

        return null
    }

    fun getRates(): Rates {
        val rates: ArrayList<Float> = ArrayList()
        for (currency: Currency in currencies) {
            rates.add(currency.defaultValue)
        }
        return Rates(versionDate, Exchanges(rates))
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(version)
        parcel.writeLong(versionDate.time)
        parcel.writeInt(totalCurrencies)
        parcel.writeTypedArray(currencies, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Currencies> {
        override fun createFromParcel(parcel: Parcel): Currencies {
            return Currencies(parcel)
        }

        override fun newArray(size: Int): Array<Currencies?> {
            return arrayOfNulls(size)
        }
    }
}