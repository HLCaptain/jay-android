---
sidebar_position: 2
---

# Opening the Project

1. Fork the project and clone it to your local machine.
2. Open the project in Android Studio.
3. Android Studio will automatically create `local.properties` file with Android SDK path. (If not, create it manually.)

## Building the project

This is a multi-step process, which requires you to have multiple accounts from Google and Mapbox.

### Aquire the necessary keys

You will need the following **confidential** keys and files to build the project:

- Firebase `google-services.json`. [Tutorial here](firebase-project-setup.md).
- Mapbox `MAPBOX_ACCESS_TOKEN`, `MAPBOX_DOWNLOADS_TOKEN`, `SDK_REGISTRY_TOKEN`. [Tutorial here](mapbox-app-setup.md).
- AdMob `ADMOB_APPLICATION_ID` and `ADMOB_AD_UNIT_ID`. [Tutorial here](admob-setup.md).
- Release and debug keys for signing the app, which can be created using Android Studio.

#### Referencing release and debug keys

Reference your release and debug keys in `local.properties`.

```properties
RELEASE_KEYSTORE_PASSWORD=your_keystore_password
RELEASE_KEY_PASSWORD=your_key_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PATH=your_key_path

DEBUG_KEYSTORE_PASSWORD=your_keystore_password
DEBUG_KEY_PASSWORD=your_key_password
DEBUG_KEY_ALIAS=your_key_alias
DEBUG_KEY_PATH=your_key_path
```

### Project setup

1. Place your keys into the `local.properties` file and files under `app` directory in the root folder.
2. Change the package name to your own (what you set up in Firebase).

### Running the project

1. Sync the project with Gradle files.
2. Build the project.
3. Run the project on an emulator or a physical device.

Congratulations! You have successfully built and ran the project. 🎉
