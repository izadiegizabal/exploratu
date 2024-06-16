package xyz.izadi.exploratu.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import xyz.izadi.exploratu.currencies.data.PrepopulateRatesCallback
import xyz.izadi.exploratu.currencies.data.RatesDatabase
import xyz.izadi.exploratu.currencies.data.api.ExchangeRatesAPI
import javax.inject.Singleton

private const val RATES_DB_NAME = "rates.db"

@Module
@InstallIn(SingletonComponent::class)
object RatesModule {
    private val jsonParser = Json {
        ignoreUnknownKeys = true
    }

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
    fun providesRatesRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://cdn.jsdelivr.net/npm/@fawazahmed0/")
        .addConverterFactory(
            jsonParser.asConverterFactory("application/json; charset=UTF8".toMediaType())
        )
        .build()

    @[Singleton Provides]
    fun providesRatesApi(
        retrofit: Retrofit
    ): ExchangeRatesAPI = retrofit.create(ExchangeRatesAPI::class.java)

}
