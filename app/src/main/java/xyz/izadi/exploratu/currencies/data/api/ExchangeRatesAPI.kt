package xyz.izadi.exploratu.currencies.data.api

import retrofit2.Response
import retrofit2.http.GET
import xyz.izadi.exploratu.currencies.data.models.Rates

interface ExchangeRatesAPI {
    @GET("latest/")
    suspend fun getLatestRates(): Response<Rates>
}