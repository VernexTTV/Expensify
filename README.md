# Expense Tracker Android App

A simple and intuitive Android application for tracking daily expenses with detailed payment information.

## Features

- **Add Expenses**: Record expenses with date, amount, and payment details
- **Payment Modes**: 
  - Credit Card (with bank name)
  - Debit Card (with bank name)
  - UPI (with app name like Google Pay, PhonePe, Paytm, etc.)
  - Cash
- **View History**: See all expenses in a scrollable list
- **Edit & Delete**: Modify or remove expense entries
- **Total Calculation**: Automatically calculates total expenses
- **Local Storage**: All data stored locally using Room database
- **Offline Access**: Works completely offline

## Requirements

- Android device running Android 7.0 (API 24) or higher
- No internet connection required

## Building the APK

### Prerequisites

You need to install Android Studio to build the APK:

1. Download Android Studio from: https://developer.android.com/studio
2. Install Android Studio (follow the installer wizard)
3. Launch Android Studio and complete the initial setup (this will download the Android SDK)

### Building Steps

1. Open Android Studio
2. Click **File** > **Open** and select the `ExpenseTracker` folder
3. Wait for Android Studio to download dependencies and sync the project (first time may take 5-10 minutes)
4. Once sync is complete, click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**
5. Wait for the build to complete (you'll see a notification at the bottom)
6. Click **locate** in the notification to open the folder with the APK
7. The APK file will be named: `app-debug.apk`

### Alternative: Command Line Build

If you have Android Studio installed, you can also build from command line:

**On Windows:**
```cmd
cd ExpenseTracker
gradlew.bat assembleDebug
```

**On Mac/Linux:**
```bash
cd ExpenseTracker
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Installing the APK

1. Transfer the APK file to your Android phone
2. Open the APK file on your phone
3. If prompted, allow installation from unknown sources
4. Tap "Install"

## Usage

1. **Add an Expense**: Tap the + button at the bottom right
2. **Fill Details**:
   - Select date
   - Enter amount
   - Choose payment mode
   - For card payments: enter bank name
   - For UPI: enter app name (Google Pay, PhonePe, etc.)
   - Optionally add a description
3. **Save**: Tap Save button
4. **View Expenses**: All expenses are shown on the main screen
5. **Edit**: Tap the edit icon on any expense
6. **Delete**: Tap the delete icon and confirm

## Data Storage

All expense data is stored locally on the device using SQLite database (Room). Data persists even after closing the app and will remain until the app is uninstalled.

## Privacy

This app does NOT:
- Require internet connection
- Send data to any server
- Track user behavior
- Display advertisements

All data stays on your device.

## Technical Details

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **UI**: Material Design Components
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

## Version

Version 1.0
