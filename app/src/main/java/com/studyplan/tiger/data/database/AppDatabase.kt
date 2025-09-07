package com.studyplan.tiger.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.studyplan.tiger.data.dao.StudyPlanDao
import com.studyplan.tiger.data.dao.PlanRecordDao
import com.studyplan.tiger.data.entity.StudyPlan
import com.studyplan.tiger.data.entity.PlanRecord

/**
 * 应用数据库
 */
@Database(
    entities = [StudyPlan::class, PlanRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun studyPlanDao(): StudyPlanDao
    abstract fun planRecordDao(): PlanRecordDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_plan_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}