package illyan.jay.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CoroutineDispatcherIO

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CoroutineDispatcherMain