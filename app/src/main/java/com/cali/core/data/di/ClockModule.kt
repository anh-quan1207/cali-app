package com.cali.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** Đồng hồ tiêm vào để test cố định được "hôm nay". Code theo lịch thật (CLAUDE.md). */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {
    @Provides fun clock(): Clock = Clock.systemDefaultZone()
}
