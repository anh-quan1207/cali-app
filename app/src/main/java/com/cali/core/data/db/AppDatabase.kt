package com.cali.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cali.core.data.db.dao.AiCallLogDao
import com.cali.core.data.db.dao.BackupDao
import com.cali.core.data.db.dao.ChatMessageDao
import com.cali.core.data.db.dao.FoodEntryDao
import com.cali.core.data.db.dao.GoalSnapshotDao
import com.cali.core.data.db.dao.MyFoodDao
import com.cali.core.data.db.dao.UserProfileDao
import com.cali.core.data.db.dao.WaterEntryDao
import com.cali.core.data.db.dao.WeeklyReviewDao
import com.cali.core.data.db.dao.WeightEntryDao
import com.cali.core.data.db.entity.AiCallLogEntity
import com.cali.core.data.db.entity.ChatMessageEntity
import com.cali.core.data.db.entity.FoodEntryEntity
import com.cali.core.data.db.entity.GoalSnapshotEntity
import com.cali.core.data.db.entity.MyFoodEntity
import com.cali.core.data.db.entity.UserProfileEntity
import com.cali.core.data.db.entity.WaterEntryEntity
import com.cali.core.data.db.entity.WeeklyReviewEntity
import com.cali.core.data.db.entity.WeightEntryEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        UserProfileEntity::class,
        GoalSnapshotEntity::class,
        WeightEntryEntity::class,
        FoodEntryEntity::class,
        MyFoodEntity::class,
        WaterEntryEntity::class,
        ChatMessageEntity::class,
        AiCallLogEntity::class,
        WeeklyReviewEntity::class,
    ],
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun goalSnapshotDao(): GoalSnapshotDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun myFoodDao(): MyFoodDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun aiCallLogDao(): AiCallLogDao
    abstract fun weeklyReviewDao(): WeeklyReviewDao
    abstract fun backupDao(): BackupDao

    companion object {
        const val NAME = "cali.db"
    }
}
