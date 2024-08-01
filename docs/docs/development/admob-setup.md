---
sidebar_position: 5
---

# AdMob Setup

## Create AdMob App

1. Sign In to [Google AdMob](https://apps.admob.com/).
2. Link [Firebase](https://console.firebase.google.com/) to AdMob.
3. Link [Google Ads](https://ads.google.com/) to AdMob.
4. From side bar, click on `Apps` and then `Add App`. Fill in the details.

Congratulations! You have created an AdMob app.

## Add AdMob App ID to the Project

1. Select `Apps` and then `View all apps`.
2. Copy the App ID into `local.properties` with the key `ADMOB_APPLICATION_ID`.

Congratulations! You have connected your AdMob app to the project.

## Create Ad Units

Without Ad Units, you can't show ads in your app. Here's how to create them:

1. Click on the app you just created.
2. Click on `Ad Units` and then `Add Ad Unit`. Fill in the details.
3. You can copy the Ad Unit ID from the `Ad Units` page and paste it into `Firebase Remote Config`.

You can access your Ad Unit IDs from [Firebase Remote Config](https://firebase.google.com/docs/remote-config/) and use them to initialize any Ads.
