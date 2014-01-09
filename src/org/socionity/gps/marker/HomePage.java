package org.socionity.gps.marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class HomePage extends Activity {
	Button continue_button;
	DbManager db_man;
	SocionityGPSApplication app;
	long last_record_id;
	String select;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_page);
		continue_button = (Button) findViewById(R.id.button_continue);
		app = (SocionityGPSApplication) getApplicationContext();
		db_man = app.db_man;
	}

	@Override
	protected void onResume() {
		super.onResume();
		last_record_id = db_man.get_last_record_id();
		if (last_record_id == 0)
			continue_button.setEnabled(false);
		else
			continue_button.setEnabled(true);

	}

	/* Exit */
	public void click_exit(View v) {
		this.finish();
	}

	/* SendToServer activity is evoked where one can upload the data to DB on the server */
	public void click_send(View v) {
		Intent intent = new Intent(this, SendToServer.class);
		startActivity(intent);	}

	/* Manage existing recordings */
	public void click_manage(View v) {
		Intent intent = new Intent(this, ManageRecordings.class);
		startActivity(intent);
	}

	public static void coming_soon(Context context) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle("Coming Soon..");
		alertDialog
				.setMessage("This feature is not yet implemented , and is coming soon");
		alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
		alertDialog.show();
	}

	/* Create a new recording page where one can set Project Name and its type */
	public void click_recordnew(View v) {
		AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setTitle(R.string.start_new_recording);
		dialog.setMessage(getResources().getString(R.string.new_recording_msg));
		LinearLayout lila = new LinearLayout(this);
		final Context current_context = this;
		final EditText ed = new EditText(this);
		final Spinner spinner = new Spinner(this);

		// Creating an adapter for type of project with values in the spinner (dropdown)
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.type_array, android.R.layout.simple_spinner_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
		        // An item was selected. You can retrieve the selected item using
		        select=parent.getItemAtPosition(pos).toString();
		        
		    }

		    public void onNothingSelected(AdapterView<?> parent) {
		        select="";
		    	// Another interface callback
		    }
		});
		ed.setHint("Recording Name");
		Time t = new Time();
		t.setToNow();
		ed.setText(t.format("%d/%m/%Y %l:%M %p"));
		lila.addView(ed);
		lila.addView(spinner);
		dialog.setView(lila);
		DialogInterface.OnClickListener click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					RecordingEntry entry= new RecordingEntry(ed.getText().toString(),select,0);
					Log.d("dropdown", select);
					last_record_id = db_man.add_recording(entry);
					db_man.set_last_record_id(last_record_id);
					Intent intent = new Intent(current_context,
							MainActivity.class);
					startActivity(intent);
				}
			}
		};
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", click_listener);
		dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				click_listener);
		dialog.show();
	}

	public void click_recordcontinue(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}
