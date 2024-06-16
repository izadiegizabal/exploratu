package xyz.izadi.exploratu.currencies.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Exchanges(
    @PrimaryKey(autoGenerate = true) val table_id: Int = 0,
    @SerialName("aud") val AUD: Float = 1f,
    @SerialName("bgn") val BGN: Float = 1f,
    @SerialName("brl") val BRL: Float = 1f,
    @SerialName("cad") val CAD: Float = 1f,
    @SerialName("chf") val CHF: Float = 1f,
    @SerialName("cny") val CNY: Float = 1f,
    @SerialName("czk") val CZK: Float = 1f,
    @SerialName("dkk") val DKK: Float = 1f,
    @SerialName("eur") val EUR: Float = 1f,
    @SerialName("gbp") val GBP: Float = 1f,
    @SerialName("hkd") val HKD: Float = 1f,
    @SerialName("hrk") val HRK: Float = 1f,
    @SerialName("huf") val HUF: Float = 1f,
    @SerialName("idr") val IDR: Float = 1f,
    @SerialName("ils") val ILS: Float = 1f,
    @SerialName("inr") val INR: Float = 1f,
    @SerialName("isk") val ISK: Float = 1f,
    @SerialName("jpy") val JPY: Float = 1f,
    @SerialName("krw") val KRW: Float = 1f,
    @SerialName("mxn") val MXN: Float = 1f,
    @SerialName("myr") val MYR: Float = 1f,
    @SerialName("nok") val NOK: Float = 1f,
    @SerialName("nzd") val NZD: Float = 1f,
    @SerialName("php") val PHP: Float = 1f,
    @SerialName("pln") val PLN: Float = 1f,
    @SerialName("ron") val RON: Float = 1f,
    @SerialName("rub") val RUB: Float = 1f,
    @SerialName("sek") val SEK: Float = 1f,
    @SerialName("sgd") val SGD: Float = 1f,
    @SerialName("thb") val THB: Float = 1f,
    @SerialName("try") val TRY: Float = 1f,
    @SerialName("usd") val USD: Float = 1f,
    @SerialName("zar") val ZAR: Float = 1f,
    @SerialName("aed") val AED: Float = 1f,
    @SerialName("afn") val AFN: Float = 1f,
    @SerialName("all") val ALL: Float = 1f,
    @SerialName("amd") val AMD: Float = 1f,
    @SerialName("ang") val ANG: Float = 1f,
    @SerialName("aoa") val AOA: Float = 1f,
    @SerialName("ars") val ARS: Float = 1f,
    @SerialName("awg") val AWG: Float = 1f,
    @SerialName("azn") val AZN: Float = 1f,
    @SerialName("bam") val BAM: Float = 1f,
    @SerialName("bbd") val BBD: Float = 1f,
    @SerialName("bdt") val BDT: Float = 1f,
    @SerialName("bhd") val BHD: Float = 1f,
    @SerialName("bif") val BIF: Float = 1f,
    @SerialName("bmd") val BMD: Float = 1f,
    @SerialName("bob") val BOB: Float = 1f,
    @SerialName("bsd") val BSD: Float = 1f,
    @SerialName("btn") val BTN: Float = 1f,
    @SerialName("bwp") val BWP: Float = 1f,
    @SerialName("byn") val BYN: Float = 1f,
    @SerialName("bzd") val BZD: Float = 1f,
    @SerialName("cdf") val CDF: Float = 1f,
    @SerialName("clp") val CLP: Float = 1f,
    @SerialName("cop") val COP: Float = 1f,
    @SerialName("crc") val CRC: Float = 1f,
    @SerialName("cup") val CUP: Float = 1f,
    @SerialName("cve") val CVE: Float = 1f,
    @SerialName("dop") val DOP: Float = 1f,
    @SerialName("dzd") val DZD: Float = 1f,
    @SerialName("egp") val EGP: Float = 1f,
    @SerialName("ern") val ERN: Float = 1f,
    @SerialName("etb") val ETB: Float = 1f,
    @SerialName("fjd") val FJD: Float = 1f,
    @SerialName("fkp") val FKP: Float = 1f,
    @SerialName("gel") val GEL: Float = 1f,
    @SerialName("ghs") val GHS: Float = 1f,
    @SerialName("gmd") val GMD: Float = 1f,
    @SerialName("gnf") val GNF: Float = 1f,
    @SerialName("gtq") val GTQ: Float = 1f,
    @SerialName("gyd") val GYD: Float = 1f,
    @SerialName("hnl") val HNL: Float = 1f,
    @SerialName("htg") val HTG: Float = 1f,
    @SerialName("iqd") val IQD: Float = 1f,
    @SerialName("irr") val IRR: Float = 1f,
    @SerialName("jmd") val JMD: Float = 1f,
    @SerialName("jod") val JOD: Float = 1f,
    @SerialName("kes") val KES: Float = 1f,
    @SerialName("kgs") val KGS: Float = 1f,
    @SerialName("khr") val KHR: Float = 1f,
    @SerialName("kmf") val KMF: Float = 1f,
    @SerialName("kpw") val KPW: Float = 1f,
    @SerialName("kwd") val KWD: Float = 1f,
    @SerialName("kyd") val KYD: Float = 1f,
    @SerialName("kzt") val KZT: Float = 1f,
    @SerialName("lak") val LAK: Float = 1f,
    @SerialName("lbp") val LBP: Float = 1f,
    @SerialName("lkr") val LKR: Float = 1f,
    @SerialName("lrd") val LRD: Float = 1f,
    @SerialName("lsl") val LSL: Float = 1f,
    @SerialName("lyd") val LYD: Float = 1f,
    @SerialName("mad") val MAD: Float = 1f,
    @SerialName("mdl") val MDL: Float = 1f,
    @SerialName("mga") val MGA: Float = 1f,
    @SerialName("mkd") val MKD: Float = 1f,
    @SerialName("mmk") val MMK: Float = 1f,
    @SerialName("mnt") val MNT: Float = 1f,
    @SerialName("mop") val MOP: Float = 1f,
    @SerialName("mur") val MUR: Float = 1f,
    @SerialName("mvr") val MVR: Float = 1f,
    @SerialName("mwk") val MWK: Float = 1f,
    @SerialName("mzn") val MZN: Float = 1f,
    @SerialName("nad") val NAD: Float = 1f,
    @SerialName("ngn") val NGN: Float = 1f,
    @SerialName("nio") val NIO: Float = 1f,
    @SerialName("npr") val NPR: Float = 1f,
    @SerialName("omr") val OMR: Float = 1f,
    @SerialName("pab") val PAB: Float = 1f,
    @SerialName("pen") val PEN: Float = 1f,
    @SerialName("pgk") val PGK: Float = 1f,
    @SerialName("pkr") val PKR: Float = 1f,
    @SerialName("pyg") val PYG: Float = 1f,
    @SerialName("qar") val QAR: Float = 1f,
    @SerialName("rsd") val RSD: Float = 1f,
    @SerialName("rwf") val RWF: Float = 1f,
    @SerialName("sar") val SAR: Float = 1f,
    @SerialName("sbd") val SBD: Float = 1f,
    @SerialName("scr") val SCR: Float = 1f,
    @SerialName("sdg") val SDG: Float = 1f,
    @SerialName("shp") val SHP: Float = 1f,
    @SerialName("sll") val SLL: Float = 1f,
    @SerialName("sos") val SOS: Float = 1f,
    @SerialName("srd") val SRD: Float = 1f,
    @SerialName("syp") val SYP: Float = 1f,
    @SerialName("szl") val SZL: Float = 1f,
    @SerialName("tjs") val TJS: Float = 1f,
    @SerialName("tmt") val TMT: Float = 1f,
    @SerialName("tnd") val TND: Float = 1f,
    @SerialName("top") val TOP: Float = 1f,
    @SerialName("ttd") val TTD: Float = 1f,
    @SerialName("twd") val TWD: Float = 1f,
    @SerialName("tzs") val TZS: Float = 1f,
    @SerialName("uah") val UAH: Float = 1f,
    @SerialName("ugx") val UGX: Float = 1f,
    @SerialName("uyu") val UYU: Float = 1f,
    @SerialName("uzs") val UZS: Float = 1f,
    @SerialName("vnd") val VND: Float = 1f,
    @SerialName("vuv") val VUV: Float = 1f,
    @SerialName("wst") val WST: Float = 1f,
    @SerialName("xaf") val XAF: Float = 1f,
    @SerialName("xcd") val XCD: Float = 1f,
    @SerialName("xof") val XOF: Float = 1f,
    @SerialName("xpf") val XPF: Float = 1f,
    @SerialName("yer") val YER: Float = 1f,
    @SerialName("zmw") val ZMW: Float = 1f,
    @SerialName("zwl") val ZWL: Float = 1f
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
