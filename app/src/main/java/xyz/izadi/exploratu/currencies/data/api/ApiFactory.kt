package xyz.izadi.exploratu.currencies.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.exchangeratesapi.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val exchangeRatesAPI: ExchangeRatesAPI = retrofit().create(ExchangeRatesAPI::class.java)
}