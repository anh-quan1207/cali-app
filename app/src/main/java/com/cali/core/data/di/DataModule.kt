package com.cali.core.data.di

import android.content.Context
import androidx.room.Room
import com.cali.core.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides fun userProfileDao(db: AppDatabase) = db.userProfileDao()
    @Provides fun goalSnapshotDao(db: AppDatabase) = db.goalSnapshotDao()
    @Provides fun weightEntryDao(db: AppDatabase) = db.weightEntryDao()
    @Provides fun foodEntryDao(db: AppDatabase) = db.foodEntryDao()
    @Provides fun myFoodDao(db: AppDatabase) = db.myFoodDao()
    @Provides fun waterEntryDao(db: AppDatabase) = db.waterEntryDao()
    @Provides fun chatMessageDao(db: AppDatabase) = db.chatMessageDao()
    @Provides fun aiCallLogDao(db: AppDatabase) = db.aiCallLogDao()
    @Provides fun weeklyReviewDao(db: AppDatabase) = db.weeklyReviewDao()
}
