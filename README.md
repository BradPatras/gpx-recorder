# gpx-recorder

Android app to record gps routes in the background and allow user to export routes to GPX files

## Libs and stuff 

- [Realm](https://www.realm.io) for data persistence
- [Dexter](https://github.com/Karumi/Dexter) for permissions
- [FusedLocationProvider](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient) for location services
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/) for file writing
- [Kotlin](https://kotlinlang.org/) for kicks
- [RxJava](https://github.com/ReactiveX/RxJava) for handling everything asynchronous

## Releases
### - 1.6 | Nov 3, 2018
- Added App Shortcut for starting a new recording
- Use localized date and time formats
- Added export button to details page
- Added warning to rooted users that the app may not function properly

### - 1.5 | Oct 21, 2018
- Reduced app size by 65%
- Fixed more issues with Android 9

### - 1.4 | Oct 16, 2018
- Revert app bundle and apk size changes due to build crashes
- Fix Android 9 issues

### - 1.4 | Oct 15, 2018
- Updates for Android Pie
- Fix for rooted devices

### - 1.3 | Oct 14, 2018
- Fix for crash on detail page on specific devices
- Reduced app install size by about 65%

### - 1.2 | Sep 8, 2018
- Fixed an issue where the app was taking a large amount of disk space
- Misc. improvements and optimizations

### - 1.1 | Aug 16, 2018
- Rewrote the route list to be more reliable. 
- Added additional error handling for devices that cannot initialize the local database.
- Fixed crashes

### - 1.0 | Jul 5, 2018
- Initial release!
