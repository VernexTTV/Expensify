# 🔍 Sentry.io Crash Reporting - Setup Guide

## ✅ What Was Added

Sentry.io SDK has been integrated to automatically capture and report all crashes with full stack traces.

---

## 🚀 Quick Setup (5 minutes)

### Step 1: Create Sentry Account
1. Go to: **https://sentry.io/signup/**
2. Sign up for a free account (100k events/month free)
3. Choose "Android" when creating your first project

### Step 2: Get Your DSN
After creating the project, Sentry will show you a DSN that looks like:
```
https://abc123def456@o1234567.ingest.sentry.io/9876543
```

### Step 3: Update the DSN
Open: `app/src/main/java/com/expensetracker/ExpenseTrackerApplication.kt`

Find this line:
```kotlin
options.dsn = "https://2e3a4f5b6c7d8e9f0a1b2c3d4e5f6a7b@o4506123456789012.ingest.sentry.io/4506123456789013"
```

Replace it with YOUR actual DSN from Step 2.

### Step 4: Rebuild and Install
```bash
cd ExpenseTracker
.\gradlew.bat assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## 📱 What Happens Now

When the app crashes:
1. ✅ **Sentry automatically captures** the crash
2. ✅ **Full stack trace** is sent to Sentry dashboard
3. ✅ **Device info** (Android version, device model) is included
4. ✅ **Crash context** (what the user was doing) is captured
5. ✅ **You get notified** via email (if configured)

---

## 🔍 How to View Crash Reports

### Option 1: Sentry Dashboard
1. Go to: **https://sentry.io/**
2. Login to your account
3. Click on your "android-expense-tracker" project
4. Go to **Issues** tab
5. You'll see all crashes with:
   - Stack trace
   - Device info
   - Time of crash
   - Number of users affected
   - Breadcrumbs (what happened before crash)

### Option 2: Email Notifications
- Sentry will email you when crashes occur
- You can configure notification rules in project settings

---

## 🎯 Current Integration

### Files Modified:
✅ `app/build.gradle` - Added Sentry SDK dependency  
✅ `build.gradle` - Added Sentry Gradle plugin  
✅ `ExpenseTrackerApplication.kt` - Sentry initialization  
✅ `AndroidManifest.xml` - Added application class and Internet permission

### Configuration:
- **Debug logging**: Enabled in debug builds
- **Performance monitoring**: Enabled (100% sample rate)
- **Environment tracking**: "debug" vs "production"
- **Release tracking**: Uses app version name

---

## 🔧 What Will Crash Reports Show?

When you open the app and it crashes, Sentry will capture:

### 1. Exception Details
```
java.lang.NullPointerException: Attempt to invoke virtual method 
'void androidx.recyclerview.widget.RecyclerView.setAdapter()' on a null object reference
    at com.expensetracker.MainActivity.onCreate(MainActivity.kt:65)
```

### 2. Device Context
- Android Version: 11
- Device: Xiaomi Redmi Note 10
- RAM: 6 GB
- App Version: 1.0

### 3. User Actions (Breadcrumbs)
- 20:08:21 - App launched
- 20:08:21 - MainActivity created
- 20:08:21 - CRASH

### 4. Environment Variables
- Database version: 2
- Categories count: 0
- Expenses count: 0

---

## 🐛 Testing Sentry Integration

To verify Sentry is working, add this test crash button:

```kotlin
// In MainActivity.onCreate() - TEMPORARY TEST
val testCrashBtn = Button(this)
testCrashBtn.text = "Test Crash"
testCrashBtn.setOnClickListener {
    throw RuntimeException("Test crash for Sentry")
}
// Add this button to your layout temporarily
```

After clicking it, check your Sentry dashboard within 1 minute!

---

## 📊 Free Tier Limits

Sentry Free Plan includes:
- ✅ 100,000 events per month
- ✅ 1 project
- ✅ 1 team member
- ✅ 30 days data retention
- ✅ Email alerts
- ✅ Full stack traces

More than enough for development and testing!

---

## 🔐 Security Note

The DSN in the code is **PUBLIC** and safe to commit to GitHub. It only allows SENDING crash reports, not reading them. Only you can view the crashes in your Sentry dashboard.

---

## 🎉 Next Steps

1. **Create your Sentry account** (5 min)
2. **Update the DSN** in ExpenseTrackerApplication.kt
3. **Rebuild the APK** and push to GitHub
4. **Install on your phone** and reproduce the crash
5. **Check Sentry dashboard** - you'll see the exact issue!

---

## 📧 Support

**Sentry Documentation**: https://docs.sentry.io/platforms/android/  
**Sentry Support**: support@sentry.io

---

**With Sentry, we'll know EXACTLY why the app is crashing!** 🚀

No more guessing - full stack traces, device info, and crash context will be available instantly.
