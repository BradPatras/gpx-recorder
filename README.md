# gpx-recorder

Android app to record gps routes in the background and allow user to export routes to GPX files

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
### - 1.6 | Nov 3, 2018
- Added App Shortcut for starting a new recording
- Use localized date and time formats
- Added export button to details page
- Added warning to rooted users that the app may not function properly
<br><br>
### - 1.5 | Oct 21, 2018
- Reduced app size by 65%
- Fixed more issues with Android 9
<br><br>
### - 1.4 | Oct 16, 2018
- Revert app bundle and apk size changes due to build crashes
- Fix Android 9 issues
<br><br>
### - 1.4 | Oct 15, 2018
- Updates for Android Pie
- Fix for rooted devices
<br><br>
### - 1.3 | Oct 14, 2018
- Fix for crash on detail page on specific devices
- Reduced app install size by about 65%
<br><br>
### - 1.2 | Sep 8, 2018
- Fixed an issue where the app was taking a large amount of disk space
- Misc. improvements and optimizations
<br><br>
### - 1.1 | Aug 16, 2018
- Rewrote the route list to be more reliable. 
- Added additional error handling for devices that cannot initialize the local database.
- Fixed crashes
<br><br>
### - 1.0 | Jul 5, 2018
- Initial release!
