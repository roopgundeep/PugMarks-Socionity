package org.socionity.gps.marker;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class PhotoCapture extends Activity {
	DbManager db_man;
	SocionityGPSApplication app;
	RecordContainer record_container;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	private Uri uriSavedImage;
	String imageName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		app = (SocionityGPSApplication) getApplicationContext();
		db_man = app.db_man;
		record_container = app.current_container;
		// Intent for Image capture
		Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Intent imageIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		// record_container.add_data(RecordContainer.DataType.LOCATION,get_loc_str());
		File imagesFolder = DbManager.app_directory;
		imagesFolder.mkdirs();
		imageName = "image_" + Long.toString(db_man.get_next_id()) + ".jpg";
		Log.d("PhotoCapture", imageName);
		File image = new File(imagesFolder, imageName);
		uriSavedImage = Uri.fromFile(image);
		imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);
		startActivityForResult(imageIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}
	//decide what to do with the  image taken
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Image captured and saved to fileUri specified in the Intent
				Toast.makeText(this, "Image saved to:\n" + uriSavedImage,
						Toast.LENGTH_LONG).show();
				record_container.add_data(RecordContainer.DataType.PHOTO,
						imageName);
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}
		}
		this.finish();
	}

}
