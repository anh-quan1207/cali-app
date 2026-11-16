package com.cali.core.notify

import com.cali.core.data.repo.ReminderRescheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class NotifyModule {
    @Binds abstract fun rescheduler(impl: WaterReminderScheduler): ReminderRescheduler
}
