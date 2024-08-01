---
sidebar_position: 7
---

# Security

Jay emphasizes security in its development. This page outlines the security measures taken in the development of Jay.

## Keys in `local.properties`

Keys are only stored inside the `local.properties` file in Jay. This file is not included in the repository, and is only stored locally on the developer's machine. This file is also added to the `.gitignore` file to prevent it from being added to the repository.

Google's Gradle Secrets Plugin is used to inject the AdMob App's ID to the project's Manifest file and nothing else. This enables the project to store all confidential keys in the `local.properties` file.

## Firebase Remote Config

[Firebase Remote Config](https://firebase.google.com/docs/remote-config/) is used to store all keys and values that are not public. This includes API keys, URLs, and other values that are not public. This allows the project to keep the `strings.xml` file clean and free of any confidential information. Currently, the project does not rely on Remote Config for confidential keys, but is relying on it for AdMob Ad Unit IDs.

## Firebase Firestore Rules

Firebase Firestore rules are used to secure the database. The rules are set to only allow authenticated users to read and write to the database. This ensures that only authenticated users can access the database and that they cannot make unauthorized changes to it.

Current rules of the database:

```rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
        allow read, write: if false;
    }
    function verifyFields(required, optional) {
      let allAllowedFields = required.concat(optional);
      return request.resource.data.keys().hasAll(required) &&
        request.resource.data.keys().hasOnly(allAllowedFields);
    }
    match /users/{userId} {
      allow read, write, get, delete: if request.auth != null && request.auth.uid == userId;
    }
    match /paths/{documentId} {
        allow create: if (request.auth != null &&
        verifyFields(['ownerUUID', 'locations', 'sessionUUID', 'aggressions'], []) &&
        request.resource.data.ownerUUID is string &&
        request.resource.data.locations is bytes &&
        request.resource.data.aggressions is bytes &&
        request.resource.data.sessionUUID is string &&
        request.resource.data.ownerUUID == request.auth.uid
      );
      allow read: if request.auth != null || resource == null || resource.data == null || !('ownerUUID' in resource);
      allow read, write, list, get, delete: if request.auth != null && (resource.data == null || ('ownerUUID' in resource.data && resource.data.ownerUUID == request.auth.uid));
    }
    match /sensor/{documentId} {
        allow create: if (request.auth != null &&
        verifyFields(['ownerUUID', 'events', 'sessionUUID', 'aggressions'], []) &&
        request.resource.data.ownerUUID is string &&
        request.resource.data.events is bytes &&
        request.resource.data.sessionUUID is string &&
        request.resource.data.ownerUUID == request.auth.uid
      );
      allow read: if request.auth != null || resource == null || resource.data == null || !('ownerUUID' in resource);
      allow read, write, list, get, delete: if request.auth != null && (resource.data == null || ('ownerUUID' in resource.data && resource.data.ownerUUID == request.auth.uid));
    }
  }
}
```