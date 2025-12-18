This is a Kotlin Multiplatform project targeting Android and iOS.

* `mobile/android` is the Android app module.
* `mobile/ios` contains the iOS app + iOS framework targets.
* `shared` contains the shared KMP modules:
  - `mobile/shared/domain`
  - `mobile/shared/data`
  - `mobile/shared/presentation`
  - `mobile/shared/core` (composition root + iOS framework export)

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:
- on macOS/Linux
  ```shell
  ./gradlew :mobile:android:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :mobile:android:assembleDebug
  ```

### Build and Run iOS Application

To build and run the development version of the iOS app, use the run configuration from the run widget
in your IDE’s toolbar or open the `mobile/ios` directory in Xcode and run it from there.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…