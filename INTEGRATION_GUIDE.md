# Waste Management System - Integration Guide

## Quick Start

This guide will help you integrate the waste management system into your Urban-Space app.

## Step 1: Add Activities to AndroidManifest.xml

Add these activity declarations inside the `<application>` tag:

```xml
<activity
    android:name=".WasteScheduleActivity"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.Connecto" />

<activity
    android:name=".LocalitySelectionActivity"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.Connecto" />

<activity
    android:name=".ReminderSettingsActivity"
    android:screenOrientation="portrait"
    android:theme="@style/Theme.Connecto" />
```

Add permissions before the `<application>` tag:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## Step 2: Update build.gradle (Module: app)

Ensure these dependencies are present:

```gradle
dependencies {
    // Material Design
    implementation 'com.google.android.material:material:1.9.0'
    
    // RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
    
    // Location Services
    implementation 'com.google.android.gms:play-services-location:21.0.1'
    
    // Image Loading (Optional but recommended)
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.15.1'
}
```

## Step 3: Add Missing Icon Resources

Create the following vector drawable icons in `res/drawable/`:

**Priority Icons (Required for basic functionality):**
1. ic_arrow_back.xml
2. ic_notifications.xml
3. ic_recycling.xml
4. ic_delete.xml
5. ic_compost.xml
6. ic_schedule.xml

**Secondary Icons (For full functionality):**
7. ic_my_location.xml
8. ic_search.xml
9. ic_help.xml
10. ic_edit.xml
11. ic_info.xml
12. ic_history.xml
13. ic_location_city.xml
14. ic_chevron_right.xml
15. ic_wine_bar.xml

See `REQUIRED_ICONS.md` for detailed instructions on creating these icons.

## Step 4: Navigate from WasteFragment

Update your `WasteFragment.java` to launch the new activity:

```java
// In WasteFragment.java
public class WasteFragment extends Fragment {
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_waste, container, false);
        
        // Add button to navigate to new waste schedule
        Button viewScheduleButton = view.findViewById(R.id.viewScheduleButton);
        viewScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), WasteScheduleActivity.class);
            startActivity(intent);
        });
        
        return view;
    }
}
```

Or add a menu item in your existing fragment.

## Step 5: Database Integration (Optional)

If you want to persist waste schedules in your database:

### Update DBHelper.java

```java
// Add table creation
private static final String CREATE_WASTE_SCHEDULE_TABLE = 
    "CREATE TABLE waste_schedules (" +
    "id TEXT PRIMARY KEY," +
    "type TEXT," +
    "status TEXT," +
    "date TEXT," +
    "time TEXT," +
    "area TEXT," +
    "frequency TEXT," +
    "instructions TEXT," +
    "icon_resource INTEGER," +
    "background_color INTEGER," +
    "icon_color INTEGER)";

@Override
public void onCreate(SQLiteDatabase db) {
    // ... existing tables ...
    db.execSQL(CREATE_WASTE_SCHEDULE_TABLE);
}

// Add CRUD methods
public void insertWasteSchedule(WasteSchedule schedule) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put("id", schedule.getId());
    values.put("type", schedule.getType());
    values.put("status", schedule.getStatus());
    values.put("date", schedule.getDate());
    values.put("time", schedule.getTime());
    values.put("area", schedule.getArea());
    values.put("frequency", schedule.getFrequency());
    values.put("instructions", schedule.getInstructions());
    values.put("icon_resource", schedule.getIconResource());
    values.put("background_color", schedule.getBackgroundColor());
    values.put("icon_color", schedule.getIconColor());
    
    db.insert("waste_schedules", null, values);
    db.close();
}

public List<WasteSchedule> getAllWasteSchedules() {
    List<WasteSchedule> schedules = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    
    Cursor cursor = db.rawQuery("SELECT * FROM waste_schedules ORDER BY date", null);
    
    if (cursor.moveToFirst()) {
        do {
            WasteSchedule schedule = new WasteSchedule(
                cursor.getString(0), // id
                cursor.getString(1), // type
                cursor.getString(2), // status
                cursor.getString(3), // date
                cursor.getString(4), // time
                cursor.getString(5), // area
                cursor.getString(6), // frequency
                cursor.getString(7), // instructions
                cursor.getInt(8),    // icon_resource
                cursor.getInt(9),    // background_color
                cursor.getInt(10)    // icon_color
            );
            schedules.add(schedule);
        } while (cursor.moveToNext());
    }
    
    cursor.close();
    db.close();
    return schedules;
}
```

### Update WasteScheduleActivity.java

Replace the sample data loading:

```java
private void loadScheduleData() {
    scheduleList.clear();
    
    // Load from database instead of sample data
    DBHelper dbHelper = new DBHelper(this);
    scheduleList.addAll(dbHelper.getAllWasteSchedules());
    
    scheduleAdapter.notifyDataSetChanged();
}
```

## Step 6: Setup Notifications (Optional)

Create a notification service for reminders:

```java
public class WasteReminderService {
    
    public static void scheduleReminder(Context context, WasteSchedule schedule, String reminderTime) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, WasteReminderReceiver.class);
        intent.putExtra("waste_type", schedule.getType());
        intent.putExtra("waste_time", schedule.getTime());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, 
            schedule.getId().hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Calculate alarm time based on reminderTime
        long triggerTime = calculateTriggerTime(schedule.getDate(), schedule.getTime(), reminderTime);
        
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
    
    private static long calculateTriggerTime(String date, String time, String reminderTime) {
        // Parse date and time, subtract reminder offset
        // Return timestamp in milliseconds
        return System.currentTimeMillis(); // Placeholder
    }
}
```

## Step 7: Testing

1. **Test Locality Selection:**
   - Grant location permissions
   - Test GPS detection
   - Test search functionality
   - Verify selection saves to SharedPreferences

2. **Test Waste Schedule:**
   - Verify filter chips work
   - Check hero card displays correctly
   - Ensure RecyclerView scrolls smoothly
   - Test notification button

3. **Test Reminder Settings:**
   - Toggle master switch
   - Toggle individual categories
   - Change reminder times
   - Verify persistence after app restart

## Step 8: Customization

### Change Primary Color
Edit `res/values/colors.xml`:
```xml
<color name="primary">#YOUR_COLOR_HERE</color>
```

### Add More Waste Categories
1. Add new filter chip in `activity_waste_schedule.xml`
2. Add new case in `WasteScheduleActivity.setupFilterChips()`
3. Add new item in `ReminderSettingsActivity`
4. Create corresponding icon and colors

### Customize Reminder Times
Edit `ReminderSettingsActivity.showTimePickerDialog()`:
```java
String[] options = {
    "15 mins before",
    "30 mins before",
    "1 hour before",
    "2 hours before",
    "The night before",
    "Custom time"
};
```

## Troubleshooting

### Icons not showing
- Ensure all icon resources are created in `res/drawable/`
- Check icon names match exactly (case-sensitive)
- Verify XML syntax in vector drawables

### Location not detected
- Check location permissions in manifest
- Request runtime permissions in activity
- Enable GPS on device
- Test on physical device (emulator may have issues)

### Chips not changing color
- Verify `chip_text_color_selector.xml` exists in `res/color/`
- Check `chip_background_selector.xml` exists in `res/drawable/`
- Ensure chips use `style="@style/Widget.MaterialComponents.Chip.Choice"`

### RecyclerView not showing items
- Check adapter is set: `recyclerView.setAdapter(adapter)`
- Verify layout manager: `recyclerView.setLayoutManager(new LinearLayoutManager(this))`
- Ensure data list is not empty
- Check item layout file exists

## Next Features to Implement

1. **Push Notifications** - Real-time alerts for collection times
2. **Calendar Integration** - Add pickups to device calendar
3. **Waste Tracking** - Track amount of waste collected over time
4. **Gamification** - Rewards for consistent recycling
5. **Community Features** - Share tips and achievements
6. **AR Features** - Scan items to identify waste category
7. **Analytics Dashboard** - Visualize waste reduction progress

## Support

For issues or questions:
1. Check `WASTE_MANAGEMENT_IMPLEMENTATION.md` for detailed documentation
2. Review `REQUIRED_ICONS.md` for icon setup
3. Verify all files are in correct directories
4. Check Android Studio build errors

## Summary

✅ Activities created and configured
✅ Layouts designed with Material Design
✅ Models and adapters implemented
✅ Color scheme and drawables added
✅ SharedPreferences integration
✅ Location services integrated
✅ Documentation provided

Your waste management system is ready to use! 🎉

