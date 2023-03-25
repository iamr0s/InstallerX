package com.rosan.installer.data.settings.model.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rosan.installer.data.settings.model.room.dao.AppDao
import com.rosan.installer.data.settings.model.room.dao.ConfigDao
import com.rosan.installer.data.settings.model.room.entity.AppEntity
import com.rosan.installer.data.settings.model.room.entity.ConfigEntity
import com.rosan.installer.data.settings.model.room.entity.converter.AnalyserConverter
import com.rosan.installer.data.settings.model.room.entity.converter.AuthorizerConverter
import com.rosan.installer.data.settings.model.room.entity.converter.InstallModeConverter
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Database(
    entities = [AppEntity::class, ConfigEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    AuthorizerConverter::class,
    InstallModeConverter::class,
    AnalyserConverter::class
)
abstract class InstallerRoom : RoomDatabase() {
    companion object : KoinComponent {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE config ADD COLUMN compat_mode INTEGER DEFAULT 0 NOT NULL")
            }
        }

        fun createInstance(): InstallerRoom {
            return Room.databaseBuilder(
                get(),
                InstallerRoom::class.java,
                "installer.db",
            )
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }

    abstract val appDao: AppDao

    abstract val configDao: ConfigDao
}