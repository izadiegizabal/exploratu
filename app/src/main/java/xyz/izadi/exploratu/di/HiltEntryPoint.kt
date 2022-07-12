package xyz.izadi.exploratu.di

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import xyz.izadi.exploratu.initializer.AdmobInitializer

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltEntryPoint {
    fun inject(initializer: AdmobInitializer)
}

fun Context.getEntryPoint(): HiltEntryPoint =
    EntryPointAccessors.fromApplication(
        context = this,
        entryPoint = HiltEntryPoint::class.java
    )
