package illyan.jay

import android.content.Context
import android.hardware.SensorManager
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
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) = LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
}