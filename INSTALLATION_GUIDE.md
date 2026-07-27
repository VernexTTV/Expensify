# Installation Guide for Expense Tracker App

This guide will help you build and install the Expense Tracker app on your Android phone.

## Step 1: Install Android Studio

1. Go to your computer and open a web browser
2. Visit: **https://developer.android.com/studio**
3. Click the **Download Android Studio** button
4. Run the downloaded installer
5. Follow the installation wizard:
   - Accept the license agreement
   - Choose default installation location
   - Install all components (Android SDK, Android Virtual Device)
6. Launch Android Studio after installation
7. Complete the setup wizard (this will download required tools - takes 5-10 minutes)

## Step 2: Open the Project

1. In Android Studio, click **File** > **Open**
2. Navigate to where you have the **ExpenseTracker** folder
3. Select the **ExpenseTracker** folder and click **OK**
4. Android Studio will start loading the project
5. You'll see a message at the bottom saying "Gradle sync"
6. **Wait for the sync to complete** - First time can take 5-15 minutes as it downloads dependencies
7. When complete, you'll see "Gradle sync finished" at the bottom

## Step 3: Build the APK

1. In Android Studio, click on the menu: **Build**
2. Then: **Build Bundle(s) / APK(s)**
3. Then: **Build APK(s)**
4. Wait for the build to complete (usually 1-3 minutes)
5. You'll see a notification at the bottom-right saying "APK(s) generated successfully"
6. Click **locate** in that notification
7. A folder will open showing the APK file named: **app-debug.apk**

## Step 4: Transfer APK to Phone

### Option A: USB Cable
1. Connect your phone to the computer using a USB cable
2. On your phone, when you see the USB notification, select **File Transfer** or **MTP**
3. Copy the **app-debug.apk** file to your phone's **Downloads** folder
4. Disconnect the phone

### Option B: Email
1. Email the **app-debug.apk** file to yourself
2. Open the email on your phone and download the attachment

### Option C: Cloud Storage
1. Upload **app-debug.apk** to Google Drive, Dropbox, or WhatsApp
2. Download it from your phone

## Step 5: Install on Phone

1. On your Android phone, open the **Files** app or **Downloads** folder
2. Find and tap on **app-debug.apk**
3. You may see a warning: "Install unknown apps"
   - Tap **Settings**
   - Enable "Allow from this source"
   - Press back button
4. Tap **Install**
5. Wait for installation to complete
6. Tap **Open** to launch the app

## Step 6: Start Using the App

The app is now installed! You can find it in your app drawer.

### First Use:
1. Open the app
2. Tap the **+** button (blue circle at bottom-right)
3. Fill in expense details:
   - Select date
   - Enter amount
   - Choose payment mode (Credit Card, Debit Card, UPI, or Cash)
   - Enter bank name or UPI app name
   - Optionally add a description
4. Tap **Save**

That's it! The expense is now saved and will appear in the list.

## Troubleshooting

### "App not installed" error
- Make sure you have enough storage space on your phone
- Try uninstalling any previous version first
- Restart your phone and try again

### "Parse error" or "There was a problem parsing the package"
- The APK file might be corrupted during transfer
- Try transferring it again
- Make sure you're using Android 7.0 or higher

### Android Studio sync fails
- Make sure you have a stable internet connection
- Click **File** > **Invalidate Caches / Restart**
- Try closing and reopening Android Studio

### Build fails in Android Studio
- Make sure all updates are installed (check bottom-right for notifications)
- Click **File** > **Sync Project with Gradle Files**
- If still failing, click **Build** > **Clean Project**, then try building again

## Need Help?

If you encounter any issues:
1. Make sure your Android phone is running Android 7.0 or higher
2. Make sure your computer has at least 8GB RAM for Android Studio
3. Make sure you have at least 10GB free disk space
4. Check that you have a stable internet connection during the first setup

## Alternative: Pre-built APK

If you have trouble building the APK, you can also ask someone who has Android Studio installed to build it for you and share the APK file directly.
