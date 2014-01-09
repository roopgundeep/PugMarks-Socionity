package org.socionity.gps.marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author phinfinity
 * 
 */
public class MainActivity extends Activity implements LocationListener {
	TextView locationstatus;
	TextView loc_counter;
	TextView distance_counter;
	TextView speed_counter;
	TextView photo_count;
	TextView video_count;
	TextView audio_count;
	TextView msg_count;
	TextView total_count;
	TextView recording_id;
	LocationManager locman;
	Location current_location;
	Location prev_location;
	ImageView imgstatus;
	Button button_fetch;
	Button button_add_location;
	Button button_toggle_continuous;
	Button button_add_photo;
	Button button_add_video;
	Button button_add_audio;
	Button button_add_text;
	Button button_preview_audio;
	Button button_preview_video;
	Button button_preview_photo;
	PowerManager.WakeLock wl;
	boolean continuous_mode;
	Handler handler;
	Runnable location_status_checker;
	boolean has_location;
	long last_fix;
	static long millis_valid_location = 30000; // 30 seconds
	DbManager db_man;
	SocionityGPSApplication app;
	RecordContainer record_container;
	RecordingEntry record;
	String audioName;
	public boolean RecordState=true;
	public boolean PlayerState=true;
	public MediaRecorder mRecorder;
	public MediaPlayer mPlayer;
	public String mfileName=null;
	public String LOG_TAG="MainActivity";
	public boolean single_entry=true;
	public String check="attempt";
	int MEDIA_TYPE_IMAGE = 1;
	int MEDIA_TYPE_VIDEO = 2;
	int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	int via_fetch = 0;
	long final_total_ctr = 0;		long init_total_ctr = 0;
	
	final Context current_context=this;

	/* Getting the GPS attributes from LocationManager */
	private void set_prev_location() {
		if (record_container.mLocations.size() > 0) {
			prev_location = new Location(LocationManager.GPS_PROVIDER);
			String loc = record_container.mLocations
					.get(record_container.mLocations.size() - 1).data;
			String[] data = loc.split(",");
			prev_location.setLatitude(Double.parseDouble(data[0]));
			prev_location.setLongitude(Double.parseDouble(data[1]));
			prev_location.setAltitude(Double.parseDouble(data[3]));
		}
	}

	/* Called when the activity is first created	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"socionity_wakelock");
		app = (SocionityGPSApplication) getApplicationContext(); // Application Context ==> app
		db_man = app.db_man; // db_man ==> DbManager instance

		// Making a new recordcontainer since opening new record
		record_container = new RecordContainer(this,
				db_man.get_last_record_id());
		app.set_current_container(record_container);
		record=db_man.last_recordings(record_container.rec_id);
		Log.d("record",""+record.single_entry);
		// For other activities use app.current_container. DO NOT make a new
		// copy of Record Container
		set_prev_location();		
		setContentView(R.layout.activity_main);
		handler = new Handler();

		// Accessing xml elements by getting their ID
 	        loc_counter = (TextView) findViewById(R.id.tv_loc_counter);
		distance_counter = (TextView) findViewById(R.id.tv_distance_counter);
		speed_counter = (TextView) findViewById(R.id.tv_speed_counter);
		photo_count = (TextView) findViewById(R.id.photo_count);
		video_count = (TextView) findViewById(R.id.video_count);
		audio_count = (TextView) findViewById(R.id.audio_count);
		msg_count = (TextView) findViewById(R.id.msg_count);
		total_count = (TextView) findViewById(R.id.total_count);
		recording_id = (TextView) findViewById(R.id.recording_id);
		button_fetch = (Button) findViewById(R.id.fetch);
		button_add_location = (Button) findViewById(R.id.button_add_location);
		button_toggle_continuous = (Button) findViewById(R.id.button_set_continuous);
		button_add_photo = (Button) findViewById(R.id.button_add_photo);
		button_add_video = (Button) findViewById(R.id.button_add_video);
		button_add_audio = (Button) findViewById(R.id.button_add_audio);
		button_add_text=(Button)findViewById(R.id.button_add_msg);
		button_preview_photo=(Button) findViewById(R.id.button_add_photo);
		button_preview_video=(Button) findViewById(R.id.button_add_video);
		button_preview_audio=(Button) findViewById(R.id.button_add_audio);
		locationstatus = (TextView) findViewById(R.id.locationview);
		imgstatus = (ImageView) findViewById(R.id.imgstatus);
		locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		current_location = null;
		location_status_checker = new Runnable() {
			public void run() {
				long ctime = System.currentTimeMillis();
				if (last_fix + millis_valid_location < ctime) {
					lost_location();
				} else {
					handler.postDelayed(location_status_checker, last_fix
							+ millis_valid_location - ctime + 1000);
				}
			}
		};
	}

	@Override
	protected void onPause() {
		if (continuous_mode) {
			wl.release();
			continuous_mode = false;
		}
		super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
		super.onPause();
		locman.removeUpdates(this);
		handler.removeCallbacks(location_status_checker);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		long local_total_ctr = record_container.get_audio_count() +
				record_container.get_location_count() +
				record_container.get_photo_count() + 
				record_container.get_text_count() + 
				record_container.get_video_count();
		
		// via_fetch describes whether Activity is called by clicking the 'Fetch' button ==> via_fetch = 1
		// or whether it just resumed from another activity ==> via_fetch = 0
		if(via_fetch == 0)
		{
			//lost_location() without animation call
			has_location = false;
			button_add_photo.setEnabled(false);
			button_add_video.setEnabled(false);
			button_add_location.setEnabled(false);
			button_add_audio.setEnabled(false);
			button_add_text.setEnabled(false);
			
			imgstatus.setImageResource(R.drawable.locating_0);
			init_total_ctr = local_total_ctr;
		}
		final_total_ctr = local_total_ctr;
		total_count.setText(" Total count: " + Long.toString(final_total_ctr));		

		//Toast.makeText(this, "via_fetch = " + String.valueOf(via_fetch), Toast.LENGTH_SHORT).show();
		update_views();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	private String convert_coordinate(double cood) {
		String ret = "";
		int deg = (int) Math.floor(cood);
		int min = (int) Math.floor((cood - deg) * 60);
		int sec = (int) Math.floor((((cood - deg) * 60) - min) * 60);
		ret = deg + "°" + min + "'" + sec + "\"";
		return ret;
	}

	/* Getting location attributes from current_location     */
	private String get_loc_str() {
		if (current_location == null)
			return null;
		else {
			return current_location.getLatitude() + ","
					+ current_location.getLongitude() + ","
					+ current_location.getAccuracy() + ","
					+ current_location.getAltitude() + ","
					+ current_location.getTime() + ","
					+ current_location.getSpeed() + ","
					+ current_location.getBearing();
		}
	}

	/* If location is changed from previous location ==> onLocationChanged	 */
	@Override
	public void onLocationChanged(Location location) {
		current_location = location;
		have_location();
		if (continuous_mode == true)
			add_location(null);
	}

	/* Checks whether the GPS setting is OFF	 */
	@Override
	public void onProviderDisabled(String provider) {
		Log.d("gps", "Provider has been disabled");
		//locationstatus.setText(R.string.gpsnotavl);
		button_fetch.setText(R.string.gpsnotavl);
		stop_locating_animate();
		imgstatus.setImageResource(R.drawable.nogps);
	}

	/* Checks whether the GPS setting is ON */
	@Override
	public void onProviderEnabled(String provider) {
		Log.d("gps", "Provider has been enabled");
		button_fetch.setText(R.string.fetchingstatus);

		lost_location();

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		if (status == LocationProvider.AVAILABLE) {
			have_location();
		}
	}

	/* Animating the Image while fetching the GPS co-ordinates	 */
	public void start_locating_animate() {
		imgstatus.setImageBitmap(null);
		imgstatus.setBackgroundResource(R.animator.gpsloading);
		final AnimationDrawable gpsanimate = (AnimationDrawable) imgstatus
				.getBackground();
		if (gpsanimate != null) {
			handler.post(new Runnable() {
				public void run() {
					gpsanimate.start();
				}
			});

		}
	}

	/* Stop animating the image	 */
	public void stop_locating_animate() {
		AnimationDrawable gpsanimate = (AnimationDrawable) imgstatus
				.getBackground();
		if (gpsanimate != null)
			gpsanimate.stop();
		imgstatus.setBackgroundResource(0);
	}

	/* Once the location is fetch from the GPS ==> have_location()	 */
	public void have_location() {
		current_location = locman
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		last_fix = System.currentTimeMillis();
		handler.removeCallbacks(location_status_checker);
//		handler.postDelayed(location_status_checker, millis_valid_location + 1000);
		
//	--	Code from add_location
		// If 'Fetch' button is pressed and a new location is fetched, add it to the database
		if(via_fetch == 1 && prev_location == null)
		{
			record_container.add_data(RecordContainer.DataType.LOCATION,
					get_loc_str()); 	// add_data function adds the currently fetched point to DB
			prev_location = current_location;	// now prev_location is updated to current_location
			Toast.makeText(this, "Point Added Implicitly!", Toast.LENGTH_SHORT).show();
			via_fetch = -1;
		}
//	--		
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

	/*
	 * A multimedia object is attached with a location's co-ordinate i.e. Latitude & Longitude
	 * i.e. multimedia element w.r.t a point in geospace
	 * lost_location ==> When location is not available, disable all the buttons for adding multimedia and animate the image
	 */
	public void lost_location() {
		has_location = false;

		button_add_photo.setEnabled(false);
		button_add_video.setEnabled(false);
		button_add_location.setEnabled(false);
		button_add_audio.setEnabled(false);
		button_add_text.setEnabled(false);
		//button_add_location.setText(R.string.fetchingstatus);
		start_locating_animate();
	}

	/*
	 * Updating all the views in the XML
	 * For update_<multimedia>_view, we set its text to the respective multimedia count
	 */
	public void update_photo_view() {
		photo_count.setText(Long.toString(record_container.get_photo_count()));
	}

	public void update_video_view() {
		video_count.setText(Long.toString(record_container.get_video_count()));
	}
	public void update_audio_view(){
		audio_count.setText(Long.toString(record_container.get_audio_count()));
	}
	
	public void update_text_view(){
		msg_count.setText(Long.toString(record_container.get_text_count()));
	}
	
	// db_man.get_rec_name(<id>) ==> retrieves the name of the project with ID = <id>
	// record_container.rec_id ==> the ID of the record in the database
	public void update_rec_id(){
		recording_id.setText("Project: " + db_man.get_rec_name(record_container.rec_id));
	}
	
	/* 
	 * Updating the Location View in the XML
	 * With Attributes: Latitude, Longitude, Distance, Speed
	*/
	public void update_location_view() {
		loc_counter
				.setText(Long.toString(record_container.get_location_count()));
		String location_str = "---";
		String distance = "---";
		String speed = "---";
		if (current_location != null) {
			double latitude = current_location.getLatitude();
			double longitude = current_location.getLongitude();
			location_str = "";
			location_str += convert_coordinate(Math.abs(latitude));
			location_str += latitude > 0 ? "N" : "S";
			location_str += ", " + convert_coordinate(Math.abs(longitude));
			location_str += longitude > 0 ? "E" : "W";
			location_str += " (±" + current_location.getAccuracy() + "m)";

		// Distance between current and previously fetched point
		if (current_location != null && prev_location != null) {
			distance = Float.toString(current_location
					.distanceTo(prev_location));
		}
		}

		// Getting the speed
		if (current_location != null && current_location.hasSpeed()) {
			speed = Float.toString(current_location.getSpeed());
		}

		speed_counter.setText(speed);
		distance_counter.setText(distance);
		locationstatus.setText(location_str);
	}
    public void size_alert(){
    	if(record_container.get_location_count()==0){
    		Log.d("gps","lets see whether it works??");
    		Log.d("java1",check);
    		AlertDialog dialog = new AlertDialog.Builder(this).create();
    		dialog.setTitle("Recording Size");
    		dialog.setMessage(getResources().getString(R.string.size_alert));
    		DialogInterface.OnClickListener click_listener = new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int which) {
    					if (which == DialogInterface.BUTTON_POSITIVE ) {
    						Intent intent = new Intent(current_context,SendToServer.class);
    						startActivity(intent);
    					}
    					else{
    						db_man.update_recording(1, record_container.rec_id);
    					}
    			}
    		};
    		single_entry=false;
    		check="ANNA";
    		Log.d("java2",check);
    		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "YES", click_listener);
    		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NO",
    				click_listener);
    		dialog.show();
    		}
    }
	/**
	 * Updates all the views with data counters. (Number of locations, photos,etc.)
	 */
	public void update_views() {
		update_location_view();
		update_photo_view();
		update_video_view();
		update_audio_view();
		update_text_view();
		update_rec_id();
		if(record.single_entry == 1){
			size_alert();
		}
	}

	/**
	 * Make a check for repeating points consecutively added and add a point. If
	 * a valid View is passed (was called by a button press) then it toasts a
	 * status if point was added or was duplicate
	 * 
	 * @param v
	 */
	public void add_location(View v) {
		if (prev_location == null
				|| current_location.distanceTo(prev_location) >= 0.1) {
			record_container.add_data(RecordContainer.DataType.LOCATION,
					get_loc_str());
			prev_location = current_location;
			if (v != null)
				Toast.makeText(this, "Point Added!", Toast.LENGTH_SHORT).show();
		} else if (v != null) {
			Toast.makeText(this, "Duplicate Point!", Toast.LENGTH_SHORT).show();
		}
		update_views();
	}
	
	/*
	 * fetch() function is called when the 'Fetch' button is pressed
	 * it sets the via_fetch flag to 1
	 * and request LocationManager to fetch the GPS location
	 */
	public void fetch(View v) {
		via_fetch = 1; // setting via_fetch flag true
		continuous_mode = false;
		button_toggle_continuous.setText(R.string.start_continuous);
		locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1,
				this); // request LocationManager to fetch GPS location
		lost_location(); // sets has_location to false
		last_fix = 0;
		//locationstatus.setText(R.string.fetchingstatus);
		lost_location();
		button_fetch.setEnabled(false);
		button_fetch.setText(R.string.fetchingstatus);
		update_views();
	}

	public void preview(View v) {
		// preview
		Intent intent = new Intent(this, VectorActivity.class);
		startActivity(intent);
	}

	/* Mapping a photo (if clicked) to the recently fetch point	 */
	public void add_photo(View v) {
		add_location(null); // So that photo gets mapped to this
		// TODO Make sure to not add point if photo is not added. This is a temporary hack
		Intent intent = new Intent(this, PhotoCapture.class);
		startActivity(intent);
	}

	/* Mapping a video (if recorded) to the recently fetch point	 */
	public void add_video(View v) {
		add_location(null); // So that video gets mapped to this point
		Intent intent = new Intent(this, VideoCapture.class);
		startActivity(intent);
	}

	public void preview_photo(View v) {
		Intent intent = new Intent(this, PhotoPreview.class);
		startActivity(intent);
	}

	public void preview_video(View v) {
		// TODO add code here
	}

	/* Mapping an audio (if recorded) to the recently fetch point	 */
	public void add_audio(View v){
		add_location(null); // hack to match audio to this point
		Intent intent =new Intent(this,AudioCapture.class);
		startActivity(intent);
	}

	/* Mapping a message (if added) to the recently fetch point	 */
	public void add_msg(View v){
		add_location(null); // hack to match text to this point
		Intent intent = new Intent(this,sendText.class);
		startActivity(intent);
	}
	/*
	 private void onRecord(boolean start){
	    	if(start) {
	    		record_start();
	    	}
	    	else {
	    		record_stop();
	    	}
	    }
	    private void onPlay(boolean start){
	    	if(start){
	    		player_start();
	    	}
	    	else{
	    		player_stop();
	    	}
	    }
	    private void player_start(){
	    	mPlayer = new MediaPlayer();
	        try {
	            mPlayer.setDataSource(mfileName);
	            mPlayer.prepare();
	            mPlayer.start();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }
	    }
	    private void player_stop(){
	    	mPlayer.release();
	        mPlayer = null;
	    }
	    private void record_start(){
	    	mRecorder = new MediaRecorder();
	        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	        File audioFolder = DbManager.app_directory;
			audioFolder.mkdirs();
			audioName="/audio"+Long.toString(db_man.get_next_id())+".3gp";
			File audio = new File(audioFolder, audioName);
	        mfileName = audio.getAbsolutePath();
	        mRecorder.setOutputFile(mfileName);
	        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

	        try {
	            mRecorder.prepare();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }
	        mRecorder.start();
	        
	    }
	    private void record_stop(){
	    	record_container.add_data(RecordContainer.DataType.AUDIO,
					audioName);
	    	mRecorder.stop();
	        mRecorder.release();
	        mRecorder = null;
	    }
	    public void audio_record(View v){
	    	onRecord(RecordState);
	    	if(RecordState) {
	    		    		button_add_audio.setText("Stop Recording");
	    		    		button_preview_audio.setEnabled(false);
	    	}else{
	    		button_preview_audio.setEnabled(true);
	    		button_add_audio.setText("Start Recording");
	    		
	    	}
	    	RecordState=!RecordState;
	    }
	    public void play_last_record(View v){
	    	onPlay(PlayerState);
	    	if(PlayerState) {
	    		button_preview_audio.setText("Stop");
	    		button_add_audio.setEnabled(false);
	    	}
	    	else{
	    		button_preview_audio.setText("Play Last Record");
	    		button_add_audio.setEnabled(true);
	    	}
	    	PlayerState=!PlayerState;
	    }
	    */
	    

	/*
	 * Toggle to continuous fetch mode
	 * i.e. keep fetching the point every 30 sec (say)
	 */
	public void toggle_continuous(View v) {
		if (continuous_mode) {
			continuous_mode = false;
			button_toggle_continuous.setText(R.string.start_continuous);
			wl.release();
		} else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Really start Continuous mode?");
			alertDialog
					.setMessage("Are you sure you want to start continuous mode? Points will be recorded continuously. You can stop it by pressing this button again.");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							continuous_mode = true;
							button_toggle_continuous
									.setText(R.string.stop_continuous);
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

	public void close(View v) {
		if(final_total_ctr - init_total_ctr > 0)
			Toast.makeText(this, "Point Saved with attachments", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(this, "No multimedia attached", Toast.LENGTH_SHORT).show();
		this.finish();
	}

	public void history(View v) {
		Toast.makeText(this, "Point not saved" , Toast.LENGTH_SHORT).show();
		//db_man.delete_recording(record_container.rec_id);
		this.finish();
		//HomePage.coming_soon(this);
	}

}
