# gpx-recorder

Android app to record gps routes in the background and allow user to export routes to GPX files

[Google Play Store link](https://play.google.com/store/apps/details?id=com.iboism.gpxrecorder)

## Libs and stuff 

- [Realm](https://www.realm.io) for data persistence
- [Dexter](https://github.com/Karumi/Dexter) for permissions
- [FusedLocationProvider](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) for location services
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/) for file writing
- [Kotlin](https://kotlinlang.org/) for kicks
- [RxJava](https://github.com/ReactiveX/RxJava) for handling everything asynchronous
- [EventBus](https://github.com/greenrobot/EventBus) For assisting with Service<->App communication
<br><br>

## Releases
### - 2.4 | Feb 20, 2022
- Updated dependency versions
- Added licenses info
- Migrated from Kotlin synthetics to view binding
<br><br>
### - 2.3 | Aug 13, 2021
- Fixed time zone issue - timestamps are always in UTC now
- Reduced waypoint location max wait time to 5 seconds
<br><br>
### - 2.2 | Oct 10, 2020
- Updated target to Android 11
- Added "Save to device" option when exporting route.
- Updated background location permission request flow for Android 10 and 11
<br><br>
### - 2.1 | Dec 22, 2019
- Moved recording controls to a list cell
- Added recorder page to allow viewing route while recording
<br><br>
### - 2.0 | July 5, 2019
- Removed route previews from list
- Removed Glide library from project
- Added warnings for route deletion
- Added map type toggle
- Implemented bottom app bar and bottom menu sheet
- Added currently recording route controls to main app screen
<br><br>
### - 1.8 | March 19, 2019
- Switched all route preview loading and caching to Glide using DataFetcher and ModelLoader subclasses
- Added link to legal
<br><br>
### - 1.7 | February 23, 2019
- Swapped interval selector to Number Pickers
- Persist last selected interval as default
- Fixed memory leaks
<br><br>
### - 1.6 | November 3, 2018
- Added App Shortcut for starting a new recording
- Use localized date and time formats
- Added export button to details page
- Added warning to rooted users that the app may not function properly
<br><br>
### - 1.5 | October 21, 2018
- Reduced app size by 65%
- Fixed more issues with Android 9
<br><br>
### - 1.4 | October 16, 2018
- Revert app bundle and apk size changes due to build crashes
- Fix Android 9 issues
<br><br>
### - 1.4 | October 15, 2018
- Updates for Android Pie
- Fix for rooted devices
<br><br>
### - 1.3 | October 14, 2018
- Fix for crash on detail page on specific devices
- Reduced app install size by about 65%
<br><br>
### - 1.2 | September 8, 2018
- Fixed an issue where the app was taking a large amount of disk space
- Misc. improvements and optimizations
<br><br>
### - 1.1 | August 16, 2018
- Rewrote the route list to be more reliable. 
- Added additional error handling for devices that cannot initialize the local database.
- Fixed crashes
<br><br>
### - 1.0 | July 5, 2018
- Initial release!
