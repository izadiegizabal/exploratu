package xyz.izadi.exploratu.currencies.data.models

fun Currencies.toRates() = Rates(
    date = versionDate,
    rates = this.currencies.toList().toExchange()
)

fun List<Currency>.toExchange(): Exchanges? = runCatching {
    Exchanges().apply {
        this@toExchange.forEach { currency ->
            Exchanges::class.java.getDeclaredField(currency.code).run {
                set(this@apply, currency.defaultValue)
            }
        }
    }
}.getOrNull()
