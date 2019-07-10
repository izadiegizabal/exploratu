package xyz.izadi.exploratu.currencies.models

import android.os.Parcel
import android.os.Parcelable

data class Currency(
    val name: String,
    val code: String,
    val sign: String,
    val defaultValue: Float,
    val countries: Array<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readFloat(),
        parcel.createStringArray()
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Currency

        if (name != other.name) return false
        if (code != other.code) return false
        if (sign != other.sign) return false
        if (defaultValue != other.defaultValue) return false
        if (!countries.contentEquals(other.countries)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + sign.hashCode()
        result = 31 * result + defaultValue.hashCode()
        result = 31 * result + countries.contentHashCode()
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(code)
        parcel.writeString(sign)
        parcel.writeFloat(defaultValue)
        parcel.writeStringArray(countries)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Currency> {
        override fun createFromParcel(parcel: Parcel): Currency {
            return Currency(parcel)
        }

        override fun newArray(size: Int): Array<Currency?> {
            return arrayOfNulls(size)
        }
    }
}