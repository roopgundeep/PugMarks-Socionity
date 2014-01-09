package org.socionity.gps.marker;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

public class VideoCapture extends Activity {
	DbManager db_man;
	SocionityGPSApplication app;
	RecordContainer record_container;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	private Uri uriSavedImage;
	private Uri uriSavedVideo;
	String imageName;
	String videoName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		app = (SocionityGPSApplication) getApplicationContext();
		db_man = app.db_man;
		record_container = app.current_container;
		Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		// Intent imageIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		File videoFolder = DbManager.app_directory;

		videoFolder.mkdirs(); // <----if (resultCode == RESULT_OK) {
		//getting next unique id for the name of the video
		videoName = "video" + Long.toString(db_man.get_next_id()) + ".mp4";
		File video = new File(videoFolder, videoName);
		uriSavedVideo = Uri.fromFile(video);
		videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedVideo);
		startActivityForResult(videoIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	}

	//Image capture result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Image captured and saved to fileUri specified in the Intent
				Toast.makeText(this, "Image saved to:\n" + data.getData(),
						Toast.LENGTH_LONG).show();
				record_container.add_data(RecordContainer.DataType.VIDEO,
						videoName);
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}
		}
		this.finish();
	}

}
