package xyz.izadi.exploratu.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.izadi.exploratu.currencies.data.PrepopulateRatesCallback
import xyz.izadi.exploratu.currencies.data.RatesDatabase
import xyz.izadi.exploratu.currencies.data.api.ExchangeRatesAPI
import javax.inject.Singleton

private const val RATES_DB_NAME = "rates.db"

@Module
@InstallIn(SingletonComponent::class)
object RatesModule {
    @[Singleton Provides]
    fun providesRatesDatabase(
        @ApplicationContext context: Context,
        prepopulateRatesCallback: PrepopulateRatesCallback
    ) = Room.databaseBuilder(
        context,
        RatesDatabase::class.java,
        RATES_DB_NAME
    ).fallbackToDestructiveMigration()
        .addCallback(prepopulateRatesCallback)
        .build()

    @[Singleton Provides]
    fun providesRatesDao(db: RatesDatabase) = db.ratesDao()

    @[Singleton Provides]
    fun providesRatesGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .create()

    @[Singleton Provides]
    fun providesRatesRetrofit(
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl("https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @[Singleton Provides]
    fun providesRatesApi(
        retrofit: Retrofit
    ): ExchangeRatesAPI = retrofit.create(ExchangeRatesAPI::class.java)

}
