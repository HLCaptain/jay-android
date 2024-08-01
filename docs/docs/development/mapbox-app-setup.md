---
sidebar_position: 4
---

# Mapbox Setup

Mapbox is used for displaying maps in Jay. To use Mapbox, you need to create an account and get the necessary keys [here](https://account.mapbox.com/auth/signup/).

## Acquire the necessary keys

You will need the following **confidential** keys to build the project:

- `MAPBOX_ACCESS_TOKEN`: will initialize the Mapbox SDK during runtime.
- `MAPBOX_DOWNLOADS_TOKEN`: downloads dependencies from Mapbox servers.
- `SDK_REGISTRY_TOKEN`: used for downloading Mapbox SDKs.

The keys in Jay's setup have the scopes:

- `STYLES:TILES`
- `STYLES:READ`
- `FONTS:READ`
- `DATASETS:READ`
- `VISION:READ`
- `DOWNLOADS:READ` (all except the *default public token*)

Pay-as-you-go pricing is used in Jay, so you will be charged based on your usage, but you will get plenty of free usage for experimentation.

## Link the keys to the project

1. Place the `MAPBOX_ACCESS_TOKEN`, `MAPBOX_DOWNLOADS_TOKEN` and `SDK_REGISTRY_TOKEN` in the `local.properties` file in the root folder of the project.
