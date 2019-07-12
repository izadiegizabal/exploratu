package xyz.izadi.exploratu.currencies.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiFactory {
    private val exchangeRatesClient = OkHttpClient.Builder().build()

    fun retrofit(): Retrofit = Retrofit.Builder()
        .client(exchangeRatesClient)
        .baseUrl("https://api.exchangeratesapi.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val exchangeRatesAPI: ExchangeRatesAPI = retrofit().create(ExchangeRatesAPI::class.java)
}