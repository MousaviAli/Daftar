package com.parsaplanner.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority): String = value.name

    @TypeConverter
    fun toPriority(value: String): Priority = Priority.valueOf(value)
}

@Database(
    entities = [TaskEntity::class, NoteEntity::class, JournalEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun noteDao(): NoteDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "parsa_planner.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
    }
}
