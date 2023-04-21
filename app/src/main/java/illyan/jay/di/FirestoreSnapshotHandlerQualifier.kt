package illyan.jay.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class UserSnapshotHandler

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class PathSnapshotHandler
