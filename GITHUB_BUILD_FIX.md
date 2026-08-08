# GitHub Actions Build Fix - Menu Resource Error

## Issue
The GitHub Actions build was failing with Kotlin compilation errors:
```
e: Unresolved reference: action_theme
e: Unresolved reference: action_filter
```

## Root Cause
The `main_menu.xml` file had all menu items removed (commented out with "Menu items removed - using in-layout buttons instead"), but `MainActivity.kt` was still referencing these menu item IDs in the `onOptionsItemSelected()` method at lines 162 and 166:

```kotlin
override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
    return when (item.itemId) {
        R.id.action_theme -> {  // ❌ This ID didn't exist
            com.expensetracker.utils.ThemeManager.toggleTheme(this)
            true
        }
        R.id.action_filter -> {  // ❌ This ID didn't exist
            showFilterDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
```

## Solution Applied
Restored the missing menu items in `app/src/main/res/menu/main_menu.xml`:

### Before:
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <!-- Menu items removed - using in-layout buttons instead -->
        
</menu>
```

### After:
```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">
    
    <item
        android:id="@+id/action_filter"
        android:title="Filter"
        android:icon="@android:drawable/ic_menu_search"
        app:showAsAction="ifRoom" />
    
    <item
        android:id="@+id/action_theme"
        android:title="Toggle Theme"
        android:icon="@android:drawable/ic_menu_preferences"
        app:showAsAction="ifRoom" />
        
</menu>
```

## Files Modified
1. **`ExpenseTracker/app/src/main/res/menu/main_menu.xml`**
   - Added back the `action_filter` menu item
   - Added back the `action_theme` menu item
   - Both items use standard Android icons and are set to show in the action bar when there's room

## GitHub Actions Workflow
The workflow is properly configured at `.github/workflows/build-apk.yml`:
- ✅ Builds on push to `main` or `master` branches
- ✅ Can be triggered manually via `workflow_dispatch`
- ✅ Uses JDK 17 with Temurin distribution
- ✅ Runs `assembleDebug` to build debug APK
- ✅ Uploads the APK as an artifact with 7-day retention
- ✅ 15-minute timeout to prevent hanging builds

## Expected Result
The build should now complete successfully and produce a debug APK artifact that can be downloaded from the GitHub Actions run page.

## Testing Recommendations
1. Commit and push these changes to the repository
2. Check the GitHub Actions tab for the build status
3. Once the build succeeds, download the APK artifact
4. Install and test the APK on an Android device/emulator
5. Verify that:
   - The app launches without crashes
   - The theme toggle menu item works (in the action bar)
   - The filter menu item works (in the action bar)
   - Both menu items display correctly

## Alternative Approach (If Menu Items Are Not Needed)
If you truly don't want these menu items and prefer only in-layout buttons, you should:
1. Remove the `onCreateOptionsMenu()` method from MainActivity.kt
2. Remove the `onOptionsItemSelected()` method from MainActivity.kt
3. Add in-layout buttons for theme toggle and filter
4. Wire up the button click listeners to call `ThemeManager.toggleTheme(this)` and `showFilterDialog()`

## Date
August 8, 2026

## Related Files
- `ExpenseTracker/app/src/main/java/com/expensetracker/MainActivity.kt` (lines 162-166)
- `ExpenseTracker/app/src/main/res/menu/main_menu.xml` (modified)
- `ExpenseTracker/.github/workflows/build-apk.yml` (unchanged)
