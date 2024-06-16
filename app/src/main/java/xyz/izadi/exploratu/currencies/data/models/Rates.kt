package xyz.izadi.exploratu.currencies.data.models

import android.text.format.DateUtils
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomWarnings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import xyz.izadi.exploratu.currencies.others.Utils
import java.util.Date

@Suppress(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
@Serializable
@Entity
data class Rates(
    @Serializable(with = DateSerializer::class)
    @[PrimaryKey SerialName("date")]
    val date: Date?,
    @[Embedded SerialName("eur")]
    val rates: Exchanges?
) {
    fun convert(quantity: Float, from: String, to: String): String = rates?.let {
        // from --> euro --> to
        val fromRate = it.getRate(from) ?: return ""
        val toRate = it.getRate(to) ?: return ""
        val conversion: Float = (quantity / fromRate) * toRate
        val roundedString = Utils.round(conversion, 4).toString()
        return Utils.addCommas(roundedString)
    } ?: ""

    fun convertFloat(quantity: Float, from: String, to: String): Float = rates?.let {
        // from --> euro --> to
        val fromRate = it.getRate(from) ?: return 1.0f
        val toRate = it.getRate(to) ?: return 1.0f
        (quantity / fromRate) * toRate
    } ?: 1.0f

    fun haveBeenRefreshedToday() = date?.time?.let { DateUtils.isToday(it) } == true
}
