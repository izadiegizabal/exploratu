package xyz.izadi.exploratu.currencies.data.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.Date

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Date::class)
class DateSerializer : KSerializer<Date> {
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(value.toInstant().toString())
    }

    override fun deserialize(decoder: Decoder): Date {
        val dateString = decoder.decodeString()
        val localDate = LocalDate.parse(dateString, LocalDate.Formats.ISO)
        return Date.from(localDate.atStartOfDayIn(TimeZone.currentSystemDefault()).toJavaInstant())
    }
}

fun Currencies.toRates() = Rates(
    date = versionDate,
    rates = this.currencies.toList().toExchange()
)

fun List<Currency>.toExchange(): Exchanges? = runCatching {
    val currencyMap = this.associateBy { it.code }

    fun getDefaultValueOf(code: String): Float = currencyMap[code]?.defaultValue ?: 1.0f

    Exchanges(
        AUD = getDefaultValueOf("AUD"),
        BGN = getDefaultValueOf("BGN"),
        BRL = getDefaultValueOf("BRL"),
        CAD = getDefaultValueOf("CAD"),
        CHF = getDefaultValueOf("CHF"),
        CNY = getDefaultValueOf("CNY"),
        CZK = getDefaultValueOf("CZK"),
        DKK = getDefaultValueOf("DKK"),
        EUR = getDefaultValueOf("EUR"),
        GBP = getDefaultValueOf("GBP"),
        HKD = getDefaultValueOf("HKD"),
        HRK = getDefaultValueOf("HRK"),
        HUF = getDefaultValueOf("HUF"),
        IDR = getDefaultValueOf("IDR"),
        ILS = getDefaultValueOf("ILS"),
        INR = getDefaultValueOf("INR"),
        ISK = getDefaultValueOf("ISK"),
        JPY = getDefaultValueOf("JPY"),
        KRW = getDefaultValueOf("KRW"),
        MXN = getDefaultValueOf("MXN"),
        MYR = getDefaultValueOf("MYR"),
        NOK = getDefaultValueOf("NOK"),
        NZD = getDefaultValueOf("NZD"),
        PHP = getDefaultValueOf("PHP"),
        PLN = getDefaultValueOf("PLN"),
        RON = getDefaultValueOf("RON"),
        RUB = getDefaultValueOf("RUB"),
        SEK = getDefaultValueOf("SEK"),
        SGD = getDefaultValueOf("SGD"),
        THB = getDefaultValueOf("THB"),
        TRY = getDefaultValueOf("TRY"),
        USD = getDefaultValueOf("USD"),
        ZAR = getDefaultValueOf("ZAR"),
        AED = getDefaultValueOf("AED"),
        AFN = getDefaultValueOf("AFN"),
        ALL = getDefaultValueOf("ALL"),
        AMD = getDefaultValueOf("AMD"),
        ANG = getDefaultValueOf("ANG"),
        AOA = getDefaultValueOf("AOA"),
        ARS = getDefaultValueOf("ARS"),
        AWG = getDefaultValueOf("AWG"),
        AZN = getDefaultValueOf("AZN"),
        BAM = getDefaultValueOf("BAM"),
        BBD = getDefaultValueOf("BBD"),
        BDT = getDefaultValueOf("BDT"),
        BHD = getDefaultValueOf("BHD"),
        BIF = getDefaultValueOf("BIF"),
        BMD = getDefaultValueOf("BMD"),
        BOB = getDefaultValueOf("BOB"),
        BSD = getDefaultValueOf("BSD"),
        BTN = getDefaultValueOf("BTN"),
        BWP = getDefaultValueOf("BWP"),
        BYN = getDefaultValueOf("BYN"),
        BZD = getDefaultValueOf("BZD"),
        CDF = getDefaultValueOf("CDF"),
        CLP = getDefaultValueOf("CLP"),
        COP = getDefaultValueOf("COP"),
        CRC = getDefaultValueOf("CRC"),
        CUP = getDefaultValueOf("CUP"),
        CVE = getDefaultValueOf("CVE"),
        DOP = getDefaultValueOf("DOP"),
        DZD = getDefaultValueOf("DZD"),
        EGP = getDefaultValueOf("EGP"),
        ERN = getDefaultValueOf("ERN"),
        ETB = getDefaultValueOf("ETB"),
        FJD = getDefaultValueOf("FJD"),
        FKP = getDefaultValueOf("FKP"),
        GEL = getDefaultValueOf("GEL"),
        GHS = getDefaultValueOf("GHS"),
        GMD = getDefaultValueOf("GMD"),
        GNF = getDefaultValueOf("GNF"),
        GTQ = getDefaultValueOf("GTQ"),
        GYD = getDefaultValueOf("GYD"),
        HNL = getDefaultValueOf("HNL"),
        HTG = getDefaultValueOf("HTG"),
        IQD = getDefaultValueOf("IQD"),
        IRR = getDefaultValueOf("IRR"),
        JMD = getDefaultValueOf("JMD"),
        JOD = getDefaultValueOf("JOD"),
        KES = getDefaultValueOf("KES"),
        KGS = getDefaultValueOf("KGS"),
        KHR = getDefaultValueOf("KHR"),
        KMF = getDefaultValueOf("KMF"),
        KPW = getDefaultValueOf("KPW"),
        KWD = getDefaultValueOf("KWD"),
        KYD = getDefaultValueOf("KYD"),
        KZT = getDefaultValueOf("KZT"),
        LAK = getDefaultValueOf("LAK"),
        LBP = getDefaultValueOf("LBP"),
        LKR = getDefaultValueOf("LKR"),
        LRD = getDefaultValueOf("LRD"),
        LSL = getDefaultValueOf("LSL"),
        LYD = getDefaultValueOf("LYD"),
        MAD = getDefaultValueOf("MAD"),
        MDL = getDefaultValueOf("MDL"),
        MGA = getDefaultValueOf("MGA"),
        MKD = getDefaultValueOf("MKD"),
        MMK = getDefaultValueOf("MMK"),
        MNT = getDefaultValueOf("MNT"),
        MOP = getDefaultValueOf("MOP"),
        MUR = getDefaultValueOf("MUR"),
        MVR = getDefaultValueOf("MVR"),
        MWK = getDefaultValueOf("MWK"),
        MZN = getDefaultValueOf("MZN"),
        NAD = getDefaultValueOf("NAD"),
        NGN = getDefaultValueOf("NGN"),
        NIO = getDefaultValueOf("NIO"),
        NPR = getDefaultValueOf("NPR"),
        OMR = getDefaultValueOf("OMR"),
        PAB = getDefaultValueOf("PAB"),
        PEN = getDefaultValueOf("PEN"),
        PGK = getDefaultValueOf("PGK"),
        PKR = getDefaultValueOf("PKR"),
        PYG = getDefaultValueOf("PYG"),
        QAR = getDefaultValueOf("QAR"),
        RSD = getDefaultValueOf("RSD"),
        RWF = getDefaultValueOf("RWF"),
        SAR = getDefaultValueOf("SAR"),
        SBD = getDefaultValueOf("SBD"),
        SCR = getDefaultValueOf("SCR"),
        SDG = getDefaultValueOf("SDG"),
        SHP = getDefaultValueOf("SHP"),
        SLL = getDefaultValueOf("SLL"),
        SOS = getDefaultValueOf("SOS"),
        SRD = getDefaultValueOf("SRD"),
        SYP = getDefaultValueOf("SYP"),
        SZL = getDefaultValueOf("SZL"),
        TJS = getDefaultValueOf("TJS"),
        TMT = getDefaultValueOf("TMT"),
        TND = getDefaultValueOf("TND"),
        TOP = getDefaultValueOf("TOP"),
        TTD = getDefaultValueOf("TTD"),
        TWD = getDefaultValueOf("TWD"),
        TZS = getDefaultValueOf("TZS"),
        UAH = getDefaultValueOf("UAH"),
        UGX = getDefaultValueOf("UGX"),
        UYU = getDefaultValueOf("UYU"),
        UZS = getDefaultValueOf("UZS"),
        VND = getDefaultValueOf("VND"),
        VUV = getDefaultValueOf("VUV"),
        WST = getDefaultValueOf("WST"),
        XAF = getDefaultValueOf("XAF"),
        XCD = getDefaultValueOf("XCD"),
        XOF = getDefaultValueOf("XOF"),
        XPF = getDefaultValueOf("XPF"),
        YER = getDefaultValueOf("YER"),
        ZMW = getDefaultValueOf("ZMW"),
        ZWL = getDefaultValueOf("ZWL")
    )
}.getOrNull()
