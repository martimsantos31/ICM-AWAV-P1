# ICM-AWAV-P1

## Overview
The ICM-AWAV-P1 project is an Android application designed for event management, featuring functionalities such as user authentication, ticket purchasing, NFC payments, and real-time notifications.

## Prerequisites
Before running the application, ensure you have the following installed:
- Android Studio (latest version recommended)
- Android SDK
- Java 17 (ensure it's set up correctly in your environment)

## Getting Started

### Clone the Repository
Clone the repository to your local machine using:
```bash
git clone git@github.com:martimsantos31/ICM-AWAV-P1.git
cd ICM-AWAV-P1/PhoneApp
```

### Open the Project
1. Open Android Studio.
2. Select "Open an existing Android Studio project".
3. Navigate to the cloned repository and select the `PhoneApp` directory.

### Firebase Configuration
1. Set up a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
2. Add your Android app to the Firebase project.
3. Download the `google-services.json` file and place it in the `app/` directory of your project.


### Build and Run the Application
1. Ensure your Android device or emulator is set up.
2. In Android Studio, click on the "Run" button or use the shortcut `Shift + F10`.
3. The application should build and launch on your device/emulator.

### Connecting Sensors
- **NFC**: Ensure your device supports NFC and that it is enabled in the device settings.
- **Bluetooth**: If your application requires Bluetooth, ensure Bluetooth is enabled on your device.

### Testing
- The project includes instrumented tests located in the `app/src/androidTest` directory. You can run these tests using Android Studio's test runner.

### Troubleshooting
- If you encounter issues with Firebase, ensure that your `google-services.json` file is correctly configured and placed in the right directory.
- Check the logs in Logcat for any runtime errors or exceptions.

## Build Process
The project uses Gradle for building. You can sync the project with Gradle files by clicking on "Sync Project with Gradle Files" in Android Studio.

### Dependencies
The project includes several dependencies for Firebase, Jetpack Compose, and other libraries. Ensure you have an active internet connection to download these dependencies during the first build.
