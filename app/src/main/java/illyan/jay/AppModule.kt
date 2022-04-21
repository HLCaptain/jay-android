/*
 * Copyright (c) 2022-2022 Balázs Püspök-Kiss (Illyan)
 * Jay is a driver behaviour analytics app.
 * This file is part of Jay.
 * Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
 */

package illyan.jay

import android.content.Context
import android.hardware.SensorManager
import androidx.core.graphics.drawable.IconCompat
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
	@Provides
	fun provideAppContext(@ApplicationContext context: Context) = context

	@Provides
	@Singleton
	fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
		LocationServices.getFusedLocationProviderClient(context)

	@Provides
	@Singleton
	fun provideSensorManager(@ApplicationContext context: Context) =
		context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

	@Provides
	@Singleton
	fun provideIcon(@ApplicationContext context: Context) =
		IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground)
}