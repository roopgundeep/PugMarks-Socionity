package org.socionity.gps.marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class RecordContainer {
	public List<RecordingDataEntry> mPhotos; // Photos
	public List<RecordingDataEntry> mVideos; // Video
	public List<RecordingDataEntry> mAudios; // Audio
	public List<RecordingDataEntry> mLocations; // Location
	public List<RecordingDataEntry> mTexts; // Text
	public enum DataType {
		PHOTO, VIDEO, AUDIO, LOCATION, TEXT
	}

	DbManager db_man;
	SocionityGPSApplication app;
	long rec_id;

	/**
	 * This will make a new record entry by adding it to the database
	 * 
	 * @param context
	 * @param name
	 */
	public RecordContainer(Context context, RecordingEntry entry) {
		init_variables(context);
		rec_id = db_man.add_recording(entry);
		// load_from_db(rec_id);
		// This shouldn't be needed, db should be empty for this new id
	}

	private void load_from_db(long id) {
		List<RecordingDataEntry> l = db_man.get_data(id);
		for (RecordingDataEntry entry : l) {
			switch (DataType.valueOf(entry.data_type)) {
			case PHOTO:
				mPhotos.add(entry);
				break;
			case VIDEO:
				mVideos.add(entry);
				break;
			case AUDIO:
				mAudios.add(entry);
				break;
			case LOCATION:
				mLocations.add(entry);
				break;
			case TEXT:
				mTexts.add(entry);
				break;
			default:
				break;
			}
		}
		Collections.sort(mPhotos);
		Collections.sort(mVideos);
		Collections.sort(mAudios);
		Collections.sort(mLocations);
		Collections.sort(mTexts);
	}

	private void init_variables(Context context) {
		app = (SocionityGPSApplication) context.getApplicationContext();
		db_man = app.db_man;
		mPhotos = new ArrayList<RecordingDataEntry>();
		mVideos = new ArrayList<RecordingDataEntry>();
		mAudios = new ArrayList<RecordingDataEntry>();
		mLocations = new ArrayList<RecordingDataEntry>();
		mTexts = new ArrayList<RecordingDataEntry>();
	}

	public RecordContainer(Context context, long id) {
		init_variables(context);
		rec_id = id;
		load_from_db(id);
	}

	public void add_data(DataType type, String data) {
		RecordingDataEntry entry = new RecordingDataEntry();
		entry.record_id = this.rec_id;
		entry.time = System.currentTimeMillis();
		entry.data_type = type.toString();
		entry.data = data;
		entry.data_id = db_man.add_data(entry);
		Log.d("rec_cont","Adding " + type.toString());
		if(type == DataType.PHOTO) {
			mPhotos.add(entry);
			Log.d("rec_cont","Yep added to photos");
		}
		else if(type == DataType.VIDEO)
			mVideos.add(entry);
		else if(type == DataType.AUDIO)
			mAudios.add(entry);
		else if(type == DataType.LOCATION)
			mLocations.add(entry);
		else if(type == DataType.TEXT)
			mTexts.add(entry);
	}

	public long get_photo_count() {
		return mPhotos.size();
	}

	public long get_audio_count() {
		return mAudios.size();
	}

	public long get_video_count() {
		return mVideos.size();
	}

	public long get_location_count() {
		return mLocations.size();
	}

	public long get_text_count() {
		return mTexts.size();
	}
}
