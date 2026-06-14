package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "awake_settings")
data class AwakeSettings(
    @PrimaryKey val id: Int = 1,
    val durationMinutes: Int = -1, // -1 means infinite/infinity, otherwise 1 to 300 minutes
    val batteryThresholdEnabled: Boolean = true,
    val batteryThreshold: Int = 15, // turn off screen awake when battery goes below this percentage
    val isAwakeActive: Boolean = false // current state of the utility
)

@Dao
interface AwakeSettingsDao {
    @Query("SELECT * FROM awake_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AwakeSettings?>

    @Query("SELECT * FROM awake_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AwakeSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AwakeSettings)
}

@Database(entities = [AwakeSettings::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun awakeSettingsDao(): AwakeSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "awake_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
