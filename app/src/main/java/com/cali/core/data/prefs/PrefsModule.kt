package com.cali.core.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class SettingsStore

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class SecureStore

@Module
@InstallIn(SingletonComponent::class)
object PrefsModule {
    /** File `datastore/settings.preferences_pb`. */
    @Provides @Singleton @SettingsStore
    fun settingsStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }

    /** File `datastore/secure_prefs.preferences_pb`, loại khỏi Auto Backup (res/xml). */
    @Provides @Singleton @SecureStore
    fun secureStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("secure_prefs") }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class KeyProviderModule {
    @Binds abstract fun keyProvider(impl: AndroidKeystoreKeyProvider): KeyProvider
}
