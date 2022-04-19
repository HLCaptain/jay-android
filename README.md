# Jay Android Application

Driver behaviour analytics app.

## Functionality and architecture

Using Kotlin Coroutine Flows to sync data between views and database. Service collects sensor data
into an SQLite database using [Room](https://developer.android.com/jetpack/androidx/releases/room).
Also collects location data, which can be seen as a path
via [Google Maps](https://developers.google.com/maps/documentation).

Using [Hilt](https://dagger.dev/hilt/) for dependency injection across the application.

The backbone architecture of Jay is [RainbowCake](https://github.com/rainbowcake/rainbowcake),
developed mainly by [Márton Braun](https://github.com/zsmb13).

[Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started) is
used in navigating through fragments.

[Firebase](https://firebase.google.com/) is used to set up private keys remotely
with [Remote Config](https://firebase.google.com/docs/remote-config) and is responsible for user
authentication.

[Permission Dispatcher](https://github.com/permissions-dispatcher/PermissionsDispatcher) used to get
permissions.

## License

```text
Copyright (c) Balázs Püspök-Kiss (Illyan)
Jay is a driver behaviour analytics app.

Jay is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Jay. If not, see <https://www.gnu.org/licenses/>.
```
