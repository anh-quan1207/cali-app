package com.cali.core.ai.di

import android.util.Log
import com.cali.BuildConfig
import com.cali.core.ai.AiCallRecorder
import com.cali.core.ai.AiModelSource
import com.cali.core.ai.AiProvider
import com.cali.core.ai.ApiKeySource
import com.cali.core.ai.FoodLogTiming
import com.cali.core.ai.PromptBuilder
import com.cali.core.ai.RoomAiCallRecorder
import com.cali.core.ai.SecureApiKeySource
import com.cali.core.ai.SettingsModelSource
import com.cali.core.ai.neuralwatt.NeuralwattConfig
import com.cali.core.ai.neuralwatt.NeuralwattProvider
import com.cali.core.calc.MealTypeResolver
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class AiHttpClient

@Module
@InstallIn(SingletonComponent::class)
abstract class AiBindingsModule {
    @Binds abstract fun apiKeySource(impl: SecureApiKeySource): ApiKeySource
    @Binds abstract fun modelSource(impl: SettingsModelSource): AiModelSource
    @Binds abstract fun callRecorder(impl: RoomAiCallRecorder): AiCallRecorder
    @Binds abstract fun systemPrompt(impl: com.cali.core.ai.SystemPromptSource): com.cali.core.ai.SystemPromptProvider
}

@Module
@InstallIn(SingletonComponent::class)
object AiModule {

    @Provides @Singleton
    fun neuralwattConfig(): NeuralwattConfig = NeuralwattConfig()

    /** Log HTTP chỉ ở debug, đã che Authorization / x-api-key (06 §5). */
    @Provides @Singleton @AiHttpClient
    fun httpClient(config: NeuralwattConfig): OkHttpClient =
        NeuralwattProvider.defaultClient(config, logger = if (BuildConfig.DEBUG) { line -> Log.d("CaliAi", line) } else null)

    @Provides fun promptBuilder(): PromptBuilder = PromptBuilder()

    @Provides fun foodLogTiming(): FoodLogTiming = FoodLogTiming(MealTypeResolver())

    @Provides @Singleton
    fun aiProvider(
        @AiHttpClient client: OkHttpClient,
        keySource: ApiKeySource,
        models: AiModelSource,
        recorder: AiCallRecorder,
        config: NeuralwattConfig,
        promptBuilder: PromptBuilder,
        clock: Clock,
    ): AiProvider = NeuralwattProvider(client, keySource, models, recorder, config, promptBuilder, clock)
}
