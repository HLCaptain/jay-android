# Jay Android Application

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=HLCaptain_jay-android&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=HLCaptain_jay-android)

<p align="center">
<img alt="Jay app's icon" src="assets/JayIcon.png" width="40%"/>
</p>

In short, Jay is providing data to decide on local problems and in the transportation field. Jay should be easy to use and available to have as much impact as possible.

### About the app

- Save sensory/location data locally and sync it to the cloud
- Record a session in the background to provide seamless tracking
- Utilize its capabilities with multiple users on a single device
- Show information about your session
- UX/UI centered design, with dark mode theming

### A few technologies Jay implements

- Saving tracked data locally via [Room]
- Dependency Injection with the Compose implementation of [Hilt]
- Navigation in the app is helped by [Compose Destinations]
- Authentication, data sync, etc. is handled by [Firebase] services
- [Mapbox] is used instead of Google Maps to scale better
- Amazing (or at least not bad) UX/UI experience (development on [Figma Design Page])
- Static code analysis via [SonarCloud]

## Setup and run the project 🏃

You need to do 3 things after cloning the repository to make the app rely only on your services.

### Adding Android project to [Firebase] 🔥

You need to modify the package of the project, because [Firebase] ***WILL NOT*** let you add Jay as an Android project, because I already have a project with this package. You might as well change the name of it, but it's optional.

After you added the Android project, then you should follow the instructions [Firebase] gave you (download `google-services.json`).

### Install [Mapbox] 🗺

You will need a [Mapbox account] in order to get the private and public keys to get it up and running.

Follow the [Mapbox install] guide for further information. I placed both of my keys in the `«USER_HOME»/.gradle/gradle.properties` file.

### Set up [SonarCloud] properly

Don't forget to change your properties in the project
level `build.gradle` file.

### Few tips for security

- Never add `google-services.json` and `«USER_HOME»/.gradle/gradle.properties` into your forked repository.
- I would recommend keeping your `string.xml` file squeaky clean 🧹🧽🧼 (don't place any keys, either public or private or confidential information there), use Firebase's [Remote Config] service or place keys in `local.properties`.

## License

```text
Copyright (c) Balázs Püspök-Kiss (Illyan)

Jay is a driver behaviour analytics app.

This file is part of Jay.

Jay is free software: you can redistribute it and/or modify it under the
terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later version.
Jay is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR
A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with Jay.
If not, see <https://www.gnu.org/licenses/>.
```

[Jetpack Compose]: https://developer.android.com/jetpack/compose

[Room]: https://developer.android.com/jetpack/androidx/releases/room

[Hilt]: https://dagger.dev/hilt/

[Compose Destinations]: https://composedestinations.rafaelcosta.xyz/

[Firebase]: https://firebase.google.com/

[Mapbox]: https://www.mapbox.com/

[ML Kit]: https://developers.google.com/ml-kit

[Figma Design Page]: https://www.figma.com/file/LH7PNtnsibnbDGnAGgTQz0

[SonarCloud]: https://sonarcloud.io/

[MockK]: https://mockk.io/

[RainbowCake]: https://rainbowcake.dev/

[MVVM]: https://developer.android.com/topic/architecture

[Mapbox Install]: https://docs.mapbox.com/android/maps/guides/install/

[Mapbox Account]: https://account.mapbox.com/auth/signup/

[Remote Config]: https://firebase.google.com/docs/remote-config