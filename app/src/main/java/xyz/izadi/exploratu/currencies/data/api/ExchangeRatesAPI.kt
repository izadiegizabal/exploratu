package xyz.izadi.exploratu.currencies.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import xyz.izadi.exploratu.currencies.data.models.Rates

interface ExchangeRatesAPI {
    @GET("{date}/currencies/eur.min.json")
    suspend fun getLatestRates(
        /**
         *  Date formatted as YYYY-MM-DD or "latest"
         **/
        @Path("date") date: String = "latest"
    ): Rates
}
