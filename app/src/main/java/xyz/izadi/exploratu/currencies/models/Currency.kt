package xyz.izadi.exploratu.currencies.models

data class Currency (
    val name: String,
    val code: String,
    val sign: String,
    val defaultValue: Float,
    val countries: Array<String>
) {
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
}