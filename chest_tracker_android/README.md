# Chest Tracker (Android)

A small native Android chest counter. The total starts at **0** and is saved locally on the device using Android `SharedPreferences`. It works offline and does not request network permissions.

## Build an APK with Android Studio

1. Install Android Studio (current stable) and the Android SDK Platform 35.
2. Unzip this project.
3. In Android Studio, choose **Open** and select the `chest_tracker_android` folder.
4. Wait for Gradle sync. If prompted, install the Android Gradle Plugin / SDK components.
5. Choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
6. Android Studio will show the APK location (usually `app/build/outputs/apk/debug/app-debug.apk`).
7. Transfer the APK to your Android phone and open it. Approve installing apps from that source if Android asks.

The generated debug APK is suitable for personal installation. A Play Store release would need a signed release build.
