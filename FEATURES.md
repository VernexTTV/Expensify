# Expense Tracker - Features Overview

## App Screens

### 1. Main Screen (Home)
When your dad opens the app, he'll see:

**Top Bar:**
- App title: "Expense Tracker"
- Blue header bar

**Total Section:**
- Shows: "Total: ₹X.XX"
- Displays the sum of all expenses
- Green/teal background

**Expense List:**
- Scrollable list of all expenses
- Each expense card shows:
  - Date (e.g., "01 Jan 2024")
  - Amount in large red text (e.g., "₹1000.00")
  - Payment mode (Credit Card, Debit Card, UPI, or Cash)
  - Bank name or UPI app (if applicable)
  - Description (if added)
  - Edit button (pencil icon)
  - Delete button (trash icon)

**Empty State:**
- If no expenses yet: "No expenses yet\nTap + to add"

**Add Button:**
- Large blue + button at bottom-right corner
- Taps to add new expense

### 2. Add/Edit Expense Dialog

When tapping the + button or Edit, a dialog appears with:

**Date Selection:**
- "Date" label
- Button showing current/selected date
- Taps to open calendar picker
- Can select any past or future date

**Amount Input:**
- "Amount (₹)" label
- Text field to enter amount
- Numeric keyboard appears
- Accepts decimal values (e.g., 150.50)

**Payment Mode:**
- "Payment Mode" label
- Dropdown menu with 4 options:
  1. Credit Card
  2. Debit Card
  3. UPI
  4. Cash

**Bank/UPI Field (conditional):**
- Appears when Credit Card, Debit Card, or UPI is selected
- For Credit/Debit Card: "Bank Name" label
  - Text field: "Enter bank name"
  - Example: HDFC, SBI, ICICI, Axis, etc.
- For UPI: "UPI App" label
  - Text field: "Enter UPI app name"
  - Example: Google Pay, PhonePe, Paytm, BHIM, etc.
- Hidden when Cash is selected

**Description (Optional):**
- "Description (Optional)" label
- Multi-line text field
- Can add notes like: "Grocery shopping", "Fuel", "Medical", etc.
- Not required

**Action Buttons:**
- **Cancel**: Close dialog without saving
- **Save**: Save the expense and close dialog

### 3. Delete Confirmation

When tapping delete icon:
- Popup appears: "Delete Expense"
- Message: "Are you sure you want to delete this expense?"
- Two buttons:
  - **Delete**: Removes the expense permanently
  - **Cancel**: Keeps the expense

## User Flows

### Adding a New Expense

1. Open app
2. Tap the blue + button
3. Tap "Select Date" button and choose date from calendar
4. Enter amount (e.g., 500)
5. Select payment mode from dropdown (e.g., UPI)
6. Enter UPI app name (e.g., Google Pay)
7. Optionally add description (e.g., "Groceries")
8. Tap Save
9. Expense appears in the list
10. Total automatically updates

### Editing an Expense

1. Find the expense in the list
2. Tap the Edit icon (pencil)
3. Modify any field (date, amount, payment mode, etc.)
4. Tap Save
5. Changes reflect immediately in the list

### Deleting an Expense

1. Find the expense in the list
2. Tap the Delete icon (trash)
3. Confirm deletion by tapping "Delete"
4. Expense removed from list
5. Total automatically updates

### Viewing Expenses

- Open app to see all expenses
- Scroll up/down to view more
- Most recent expenses appear at the top
- Each expense shows full details at a glance

## Data Fields Explained

| Field | Required | Description | Example |
|-------|----------|-------------|---------|
| Date | Yes | When the expense occurred | 27 Jul 2026 |
| Amount | Yes | Money spent (in ₹) | 1500.00 |
| Payment Mode | Yes | How payment was made | Credit Card |
| Bank Name | Conditional | Required for Credit/Debit Card | HDFC Bank |
| UPI App | Conditional | Required for UPI | Google Pay |
| Description | No | Additional notes | Dinner at restaurant |

## Design Details

**Colors:**
- Primary Blue: Used for header and action buttons
- Red: Used for amount display (to indicate expense)
- Green/Teal: Used for total section
- White: Card backgrounds
- Gray: Secondary text

**Typography:**
- Large bold text for amounts
- Medium text for payment modes
- Small text for dates and details

**Icons:**
- ✏️ Pencil icon for Edit
- 🗑️ Trash icon for Delete
- ➕ Plus icon for Add

**Layout:**
- Material Design guidelines
- Card-based layout for expenses
- Floating action button for add
- Responsive to different screen sizes

## Technical Features

**Database:**
- SQLite database (via Room)
- Stores all data locally on phone
- No cloud sync
- Data persists across app restarts
- Survives phone restarts
- Only deleted when app is uninstalled

**Performance:**
- Instant loading
- Smooth scrolling
- No lag when adding/editing
- Handles thousands of expenses efficiently

**Compatibility:**
- Works on Android 7.0 and above
- Supports phones from 2016 onwards
- Tested on different screen sizes
- Works in portrait and landscape modes

**Privacy:**
- No internet permission required
- No data collection
- No analytics
- No advertisements
- Completely offline
- Data never leaves the device

## Future Enhancement Ideas

If you want to add more features later:

1. **Categories**: Group expenses by type (Food, Transport, Medical, etc.)
2. **Monthly Summary**: Show total by month
3. **Search**: Search expenses by description or amount
4. **Export**: Export data to Excel/CSV
5. **Backup**: Backup data to Google Drive
6. **Charts**: Visual graphs of spending patterns
7. **Recurring Expenses**: Auto-add monthly expenses
8. **Multiple Users**: Track expenses for family members
9. **Budget Limits**: Set monthly spending limits with alerts
10. **Receipt Photos**: Attach photos of receipts

For now, the app focuses on the core requirement: replacing the notebook with a simple digital solution that your dad can easily use to track his daily expenses.
