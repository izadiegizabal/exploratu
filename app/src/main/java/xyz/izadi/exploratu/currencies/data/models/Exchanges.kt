package xyz.izadi.exploratu.currencies.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Exchanges(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @SerializedName("aud") val AUD: Float = 1f,
    @SerializedName("bgn") val BGN: Float = 1f,
    @SerializedName("brl") val BRL: Float = 1f,
    @SerializedName("cad") val CAD: Float = 1f,
    @SerializedName("chf") val CHF: Float = 1f,
    @SerializedName("cny") val CNY: Float = 1f,
    @SerializedName("czk") val CZK: Float = 1f,
    @SerializedName("dkk") val DKK: Float = 1f,
    @SerializedName("eur") val EUR: Float = 1f,
    @SerializedName("gbp") val GBP: Float = 1f,
    @SerializedName("hkd") val HKD: Float = 1f,
    @SerializedName("hrk") val HRK: Float = 1f,
    @SerializedName("huf") val HUF: Float = 1f,
    @SerializedName("idr") val IDR: Float = 1f,
    @SerializedName("ils") val ILS: Float = 1f,
    @SerializedName("inr") val INR: Float = 1f,
    @SerializedName("isk") val ISK: Float = 1f,
    @SerializedName("jpy") val JPY: Float = 1f,
    @SerializedName("krw") val KRW: Float = 1f,
    @SerializedName("mxn") val MXN: Float = 1f,
    @SerializedName("myr") val MYR: Float = 1f,
    @SerializedName("nok") val NOK: Float = 1f,
    @SerializedName("nzd") val NZD: Float = 1f,
    @SerializedName("php") val PHP: Float = 1f,
    @SerializedName("pln") val PLN: Float = 1f,
    @SerializedName("ron") val RON: Float = 1f,
    @SerializedName("rub") val RUB: Float = 1f,
    @SerializedName("sek") val SEK: Float = 1f,
    @SerializedName("sgd") val SGD: Float = 1f,
    @SerializedName("thb") val THB: Float = 1f,
    @SerializedName("try") val TRY: Float = 1f,
    @SerializedName("usd") val USD: Float = 1f,
    @SerializedName("zar") val ZAR: Float = 1f,
    @SerializedName("aed") val AED: Float = 1f,
    @SerializedName("afn") val AFN: Float = 1f,
    @SerializedName("all") val ALL: Float = 1f,
    @SerializedName("amd") val AMD: Float = 1f,
    @SerializedName("ang") val ANG: Float = 1f,
    @SerializedName("aoa") val AOA: Float = 1f,
    @SerializedName("ars") val ARS: Float = 1f,
    @SerializedName("awg") val AWG: Float = 1f,
    @SerializedName("azn") val AZN: Float = 1f,
    @SerializedName("bam") val BAM: Float = 1f,
    @SerializedName("bbd") val BBD: Float = 1f,
    @SerializedName("bdt") val BDT: Float = 1f,
    @SerializedName("bhd") val BHD: Float = 1f,
    @SerializedName("bif") val BIF: Float = 1f,
    @SerializedName("bmd") val BMD: Float = 1f,
    @SerializedName("bob") val BOB: Float = 1f,
    @SerializedName("bsd") val BSD: Float = 1f,
    @SerializedName("btn") val BTN: Float = 1f,
    @SerializedName("bwp") val BWP: Float = 1f,
    @SerializedName("byn") val BYN: Float = 1f,
    @SerializedName("bzd") val BZD: Float = 1f,
    @SerializedName("cdf") val CDF: Float = 1f,
    @SerializedName("clp") val CLP: Float = 1f,
    @SerializedName("cop") val COP: Float = 1f,
    @SerializedName("crc") val CRC: Float = 1f,
    @SerializedName("cup") val CUP: Float = 1f,
    @SerializedName("cve") val CVE: Float = 1f,
    @SerializedName("dop") val DOP: Float = 1f,
    @SerializedName("dzd") val DZD: Float = 1f,
    @SerializedName("egp") val EGP: Float = 1f,
    @SerializedName("ern") val ERN: Float = 1f,
    @SerializedName("etb") val ETB: Float = 1f,
    @SerializedName("fjd") val FJD: Float = 1f,
    @SerializedName("fkp") val FKP: Float = 1f,
    @SerializedName("gel") val GEL: Float = 1f,
    @SerializedName("ghs") val GHS: Float = 1f,
    @SerializedName("gmd") val GMD: Float = 1f,
    @SerializedName("gnf") val GNF: Float = 1f,
    @SerializedName("gtq") val GTQ: Float = 1f,
    @SerializedName("gyd") val GYD: Float = 1f,
    @SerializedName("hnl") val HNL: Float = 1f,
    @SerializedName("htg") val HTG: Float = 1f,
    @SerializedName("iqd") val IQD: Float = 1f,
    @SerializedName("irr") val IRR: Float = 1f,
    @SerializedName("jmd") val JMD: Float = 1f,
    @SerializedName("jod") val JOD: Float = 1f,
    @SerializedName("kes") val KES: Float = 1f,
    @SerializedName("kgs") val KGS: Float = 1f,
    @SerializedName("khr") val KHR: Float = 1f,
    @SerializedName("kmf") val KMF: Float = 1f,
    @SerializedName("kpw") val KPW: Float = 1f,
    @SerializedName("kwd") val KWD: Float = 1f,
    @SerializedName("kyd") val KYD: Float = 1f,
    @SerializedName("kzt") val KZT: Float = 1f,
    @SerializedName("lak") val LAK: Float = 1f,
    @SerializedName("lbp") val LBP: Float = 1f,
    @SerializedName("lkr") val LKR: Float = 1f,
    @SerializedName("lrd") val LRD: Float = 1f,
    @SerializedName("lsl") val LSL: Float = 1f,
    @SerializedName("lyd") val LYD: Float = 1f,
    @SerializedName("mad") val MAD: Float = 1f,
    @SerializedName("mdl") val MDL: Float = 1f,
    @SerializedName("mga") val MGA: Float = 1f,
    @SerializedName("mkd") val MKD: Float = 1f,
    @SerializedName("mmk") val MMK: Float = 1f,
    @SerializedName("mnt") val MNT: Float = 1f,
    @SerializedName("mop") val MOP: Float = 1f,
    @SerializedName("mur") val MUR: Float = 1f,
    @SerializedName("mvr") val MVR: Float = 1f,
    @SerializedName("mwk") val MWK: Float = 1f,
    @SerializedName("mzn") val MZN: Float = 1f,
    @SerializedName("nad") val NAD: Float = 1f,
    @SerializedName("ngn") val NGN: Float = 1f,
    @SerializedName("nio") val NIO: Float = 1f,
    @SerializedName("npr") val NPR: Float = 1f,
    @SerializedName("omr") val OMR: Float = 1f,
    @SerializedName("pab") val PAB: Float = 1f,
    @SerializedName("pen") val PEN: Float = 1f,
    @SerializedName("pgk") val PGK: Float = 1f,
    @SerializedName("pkr") val PKR: Float = 1f,
    @SerializedName("pyg") val PYG: Float = 1f,
    @SerializedName("qar") val QAR: Float = 1f,
    @SerializedName("rsd") val RSD: Float = 1f,
    @SerializedName("rwf") val RWF: Float = 1f,
    @SerializedName("sar") val SAR: Float = 1f,
    @SerializedName("sbd") val SBD: Float = 1f,
    @SerializedName("scr") val SCR: Float = 1f,
    @SerializedName("sdg") val SDG: Float = 1f,
    @SerializedName("shp") val SHP: Float = 1f,
    @SerializedName("sll") val SLL: Float = 1f,
    @SerializedName("sos") val SOS: Float = 1f,
    @SerializedName("srd") val SRD: Float = 1f,
    @SerializedName("syp") val SYP: Float = 1f,
    @SerializedName("szl") val SZL: Float = 1f,
    @SerializedName("tjs") val TJS: Float = 1f,
    @SerializedName("tmt") val TMT: Float = 1f,
    @SerializedName("tnd") val TND: Float = 1f,
    @SerializedName("top") val TOP: Float = 1f,
    @SerializedName("ttd") val TTD: Float = 1f,
    @SerializedName("twd") val TWD: Float = 1f,
    @SerializedName("tzs") val TZS: Float = 1f,
    @SerializedName("uah") val UAH: Float = 1f,
    @SerializedName("ugx") val UGX: Float = 1f,
    @SerializedName("uyu") val UYU: Float = 1f,
    @SerializedName("uzs") val UZS: Float = 1f,
    @SerializedName("vnd") val VND: Float = 1f,
    @SerializedName("vuv") val VUV: Float = 1f,
    @SerializedName("wst") val WST: Float = 1f,
    @SerializedName("xaf") val XAF: Float = 1f,
    @SerializedName("xcd") val XCD: Float = 1f,
    @SerializedName("xof") val XOF: Float = 1f,
    @SerializedName("xpf") val XPF: Float = 1f,
    @SerializedName("yer") val YER: Float = 1f,
    @SerializedName("zmw") val ZMW: Float = 1f,
    @SerializedName("zwl") val ZWL: Float = 1f
) {
    // TODO: use a map instead of different variables
    fun getRate(from: String): Float? = when (from.uppercase()) {
        "AUD" -> AUD
        "BGN" -> BGN
        "BRL" -> BRL
        "CAD" -> CAD
        "CHF" -> CHF
        "CNY" -> CNY
        "CZK" -> CZK
        "DKK" -> DKK
        "EUR" -> EUR
        "GBP" -> GBP
        "HKD" -> HKD
        "HRK" -> HRK
        "HUF" -> HUF
        "IDR" -> IDR
        "ILS" -> ILS
        "INR" -> INR
        "ISK" -> ISK
        "JPY" -> JPY
        "KRW" -> KRW
        "MXN" -> MXN
        "MYR" -> MYR
        "NOK" -> NOK
        "NZD" -> NZD
        "PHP" -> PHP
        "PLN" -> PLN
        "RON" -> RON
        "RUB" -> RUB
        "SEK" -> SEK
        "SGD" -> SGD
        "THB" -> THB
        "TRY" -> TRY
        "USD" -> USD
        "ZAR" -> ZAR
        "AED" -> AED
        "AFN" -> AFN
        "ALL" -> ALL
        "AMD" -> AMD
        "ANG" -> ANG
        "AOA" -> AOA
        "ARS" -> ARS
        "AWG" -> AWG
        "AZN" -> AZN
        "BAM" -> BAM
        "BBD" -> BBD
        "BDT" -> BDT
        "BHD" -> BHD
        "BIF" -> BIF
        "BMD" -> BMD
        "BOB" -> BOB
        "BSD" -> BSD
        "BTN" -> BTN
        "BWP" -> BWP
        "BYN" -> BYN
        "BZD" -> BZD
        "CDF" -> CDF
        "CLP" -> CLP
        "COP" -> COP
        "CRC" -> CRC
        "CUP" -> CUP
        "CVE" -> CVE
        "DOP" -> DOP
        "DZD" -> DZD
        "EGP" -> EGP
        "ERN" -> ERN
        "ETB" -> ETB
        "FJD" -> FJD
        "FKP" -> FKP
        "GEL" -> GEL
        "GHS" -> GHS
        "GMD" -> GMD
        "GNF" -> GNF
        "GTQ" -> GTQ
        "GYD" -> GYD
        "HNL" -> HNL
        "HTG" -> HTG
        "IQD" -> IQD
        "IRR" -> IRR
        "JMD" -> JMD
        "JOD" -> JOD
        "KES" -> KES
        "KGS" -> KGS
        "KHR" -> KHR
        "KMF" -> KMF
        "KPW" -> KPW
        "KWD" -> KWD
        "KYD" -> KYD
        "KZT" -> KZT
        "LAK" -> LAK
        "LBP" -> LBP
        "LKR" -> LKR
        "LRD" -> LRD
        "LSL" -> LSL
        "LYD" -> LYD
        "MAD" -> MAD
        "MDL" -> MDL
        "MGA" -> MGA
        "MKD" -> MKD
        "MMK" -> MMK
        "MNT" -> MNT
        "MOP" -> MOP
        "MUR" -> MUR
        "MVR" -> MVR
        "MWK" -> MWK
        "MZN" -> MZN
        "NAD" -> NAD
        "NGN" -> NGN
        "NIO" -> NIO
        "NPR" -> NPR
        "OMR" -> OMR
        "PAB" -> PAB
        "PEN" -> PEN
        "PGK" -> PGK
        "PKR" -> PKR
        "PYG" -> PYG
        "QAR" -> QAR
        "RSD" -> RSD
        "RWF" -> RWF
        "SAR" -> SAR
        "SBD" -> SBD
        "SCR" -> SCR
        "SDG" -> SDG
        "SHP" -> SHP
        "SLL" -> SLL
        "SOS" -> SOS
        "SRD" -> SRD
        "SYP" -> SYP
        "SZL" -> SZL
        "TJS" -> TJS
        "TMT" -> TMT
        "TND" -> TND
        "TOP" -> TOP
        "TTD" -> TTD
        "TWD" -> TWD
        "TZS" -> TZS
        "UAH" -> UAH
        "UGX" -> UGX
        "UYU" -> UYU
        "UZS" -> UZS
        "VND" -> VND
        "VUV" -> VUV
        "WST" -> WST
        "XAF" -> XAF
        "XCD" -> XCD
        "XOF" -> XOF
        "XPF" -> XPF
        "YER" -> YER
        "ZMW" -> ZMW
        "ZWL" -> ZWL
        else -> null
    }
}
