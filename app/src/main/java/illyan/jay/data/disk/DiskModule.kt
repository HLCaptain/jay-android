/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

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