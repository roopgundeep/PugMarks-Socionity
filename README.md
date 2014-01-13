Pugmarks
==================

Pugmarks is a tool which can be used to record Location tracks and routes on a map interactively along with Photos, Videos, Audio clippings and Messages mapped to locations. The tool is aimed to help crowdsource local resources, tracks and folklore along routes.

What you need to get started
==================

User should have an android phone (version 2.2+ onwards) with the application (.apk file) installed in the device.
If you are new to Android then visit [Getting started with Android](http://developer.android.com/training/basics/firstapp/index.html) and make your first android app.

Just clone this repository to your local machine and import it as an android project in eclipse. Before launching the app in mobile, add the given .map file in your SD card and change the path to the SD card in file VectorMaps.java.

Offline Maps
=============

We will use opensource [MapsForge](http://code.google.com/p/mapsforge/) for loading maps offline to your Android phone. We will be using .map files. So to convert any .osm to .map file use osmosis.

In the file "VectorActivity.java", the function loadMap() loads the map. In file_name parameter just give the path to your .map file.
```java
public void loadMap() {
		mapView.setBuiltInZoomControls(true);//Enables Zoom functionalities
		mapView.setClickable(true); //Click-able true
		file_name = "/sdcard/VectorMaps/iiit.map"; //fileName of your Map
		file = new File(file_name);
		mapView.setMapFile(file);// set which .map file to include
		drawable.setAlpha(100);//set opaque value for a draw-able
		itemizedOverlay = new MyItem(drawable, file); 
		mapView.getOverlays().add(itemizedOverlay); // Overlaying of the Map so it becomes visible
	}
```
To put markers on the map call the function putMarker with Geopoint as the parameter
```java
public void putMarker(GeoPoint geopoint) {
		
		OverlayItem item = new OverlayItem(geopoint, "hi",
				"this is the location");
		itemizedOverlay.addItem(item); //adding the item in overlay
		mapView.getOverlays().add(itemizedOverlay); //adding the map over the layout
		mapView.invalidate(); //to redraw the view
	}
```

DataBase Manager
======
Queries

- Get list of all recordings stored in DB

```java
public List<RecordingEntry> get_recordings() {
	List<RecordingEntry> l = new ArrayList<RecordingEntry>(); 
	Cursor c = db.query(TABLE_RECORDINGS, RECORDINGS_COLUMNS, null, null,
			null, null, COLUMN_TIME);
	c.moveToFirst();
	//Loop to traverse through all record enteries and store them in a list
	while (!c.isAfterLast()) {
		RecordingEntry r = new RecordingEntry(c.getLong(0), c.getLong(1),
				c.getString(2),c.getString(3),c.getLong(4));
		l.add(r);
		c.moveToNext();
	}
	return l;
}
```

- Fetching Data of a specific point of a project from the DataBase

```java
List<RecordingDataEntry> get_data(long recording_id) {
	List<RecordingDataEntry> l = new ArrayList<RecordingDataEntry>();
	Cursor c = db.query(TABLE_DATA, DATA_COLUMNS, COLUMN_RECORD_ID + "=?",
			new String[] { Long.toString(recording_id) }, null, null, null);
	c.moveToFirst();
	while (!c.isAfterLast()) {
		RecordingDataEntry r = new RecordingDataEntry(c.getLong(0),
				c.getLong(1), c.getLong(2), c.getString(3), c.getString(4));
		l.add(r);
		c.moveToNext();
	}
	return l;
}
```
Similar functions such as 'add_recording()', 'update_recording()', 'delete_recording()', 'add_data()', etc work according to their respective functionalities.

Fetching GPS location of a point
===============

File: MainActivity.java

fetch() function is called when the 'Fetch' button is pressed
it sets the via_fetch flag to 1 and requests LocationManager to fetch the GPS location
/

```java
public void fetch(View v) {
	via_fetch = 1; // setting via_fetch flag true
	continuous_mode = false;
	button_toggle_continuous.setText(R.string.start_continuous);
	locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
			this); // request LocationManager to fetch GPS location
	lost_location(); // sets has_location to false
	last_fix = 0;
	lost_location();
	button_fetch.setEnabled(false);
	button_fetch.setText(R.string.fetchingstatus);
	update_views();
}
```

Now, when the GPS location of a point is available, i.e. ```if (status == LocationProvider.AVAILABLE)``` then have_location function is called.

Once the location is fetch from the GPS ==> have_location()

```java
public void have_location() {
	current_location = locman
			.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	last_fix = System.currentTimeMillis();
	handler.removeCallbacks(location_status_checker);
	
	// If 'Fetch' button is pressed and a new location is fetched, add it to the database
	if(via_fetch == 1 && prev_location == null)
	{
		record_container.add_data(RecordContainer.DataType.LOCATION,
				get_loc_str()); 	// add_data function adds the currently fetched point to DB
		prev_location = current_location;	// now prev_location is updated to current_location
		Toast.makeText(this, "Point Added Implicitly!", Toast.LENGTH_SHORT).show();
		via_fetch = -1;
	}

	// Since location is now obtained, now all buttons for attaching multimedia are enabled
	// viz. Add Photo, Add Video, Add Audio, Add Message
	has_location = true;
	button_add_photo.setEnabled(true);
	button_add_video.setEnabled(true);
	button_add_location.setEnabled(true);
	button_add_audio.setEnabled(true);
	button_add_text.setEnabled(true);
	button_add_location.setText(R.string.add_location);
	button_fetch.setText("Fetched");
	stop_locating_animate();
	imgstatus.setImageResource(R.drawable.located);
	update_views();
}
```

Toggle for continuous fetch mode i.e. keep fetching the point every 30 sec (say)

```java
public void toggle_continuous(View v) {
	if (continuous_mode) {
		continuous_mode = false;
		button_toggle_continuous.setText(R.string.start_continuous);
		wl.release();
	} else {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Really start Continuous mode?");
		alertDialog.setMessage("Are you sure you want to start continuous mode? Points will be recorded continuously. You can stop it by pressing this button again.");
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						continuous_mode = true;
						button_toggle_continuous.setText(R.string.stop_continuous);
						wl.acquire();
					}
				});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alertDialog.show();
	}
}
```

Multimedia Capture
=======

For capturing multimedia like Photo, Video, Audio, etc. please follow documented codes of respective multimedia format
i.e. 'PhotoCapture.java', 'VideoCapture.java', 'AudioCapture.java'
