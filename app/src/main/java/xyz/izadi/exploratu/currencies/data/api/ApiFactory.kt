package xyz.izadi.exploratu.currencies.data.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .create()

    private fun retrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val exchangeRatesAPI: ExchangeRatesAPI = retrofit().create(ExchangeRatesAPI::class.java)
}
