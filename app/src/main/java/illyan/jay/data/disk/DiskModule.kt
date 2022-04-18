package illyan.jay.data.disk

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiskModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context) = Room
        .databaseBuilder(context, JayDatabase::class.java, JayDatabase.DB_NAME)
        .fallbackToDestructiveMigration()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Timber.i("${db.path} v${db.version} created")
            }
        })
        .enableMultiInstanceInvalidation()
        .build()

    @Provides
    @Singleton
    fun provideAccelerationDao(db: JayDatabase) = db.accelerationDao()

    @Provides
    @Singleton
    fun provideLocationDao(db: JayDatabase) = db.locationDao()

    @Provides
    @Singleton
    fun provideRotationDao(db: JayDatabase) = db.rotationDao()

    @Provides
    @Singleton
    fun provideSessionDao(db: JayDatabase) = db.sessionDao()
}