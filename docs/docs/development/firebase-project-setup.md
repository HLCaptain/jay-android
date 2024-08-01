---
sidebar_position: 3
---

# Firebase and Google Cloud integration

Firebase is used for:

- Authentication
- Realtime NoSQL database
- Analytics and crashlytics
- Machine Learning model deployment
- Remote Config

Google Cloud Platform is required for OAuth2 authentication and accessing Firebase services.

## Setup Firebase

1. Create a new Firebase project [here](https://console.firebase.google.com/).
2. Add an Android app to the project. Use a distinct package name, **NOT `illyan.jay`**.
3. Add Authentication, Firestore Database, Analytics, Crashlytics, Machine Learning, Performance and Remote Config services.
4. Select `Project Settings` and download `google-services.json`.
5. Don't forget to add the SHA-1 key to the Firebase project settings. You can generate the hey with the command `./gradlew signingReport`.
6. Setup Authentication to use Google Sign-In.

## Google Cloud Platform

To use Firebase services, you need to add the SHA-1 key to the Google Cloud Platform project.

1. If not yet done, link your Firebase project to Google Cloud Platform.
2. Under `APIs & Services` ➡️ `Credentials`, select `Android key (auto created by Firebase)`.
3. Set application restrictions to `Android apps` and add the SHA-1 key with your package name.
4. If deploying to production, don't forget to publish OAuth to production. To do that, navigate to `Oath consent screen` under `Credentials` menu.
