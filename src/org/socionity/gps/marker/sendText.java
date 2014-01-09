package org.socionity.gps.marker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class sendText extends Activity{
	DbManager db_man;
	SocionityGPSApplication app;
	RecordContainer record_container;
	EditText input_box;
	TextView display_box;
	
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        app = (SocionityGPSApplication) getApplicationContext();
			db_man = app.db_man;
			record_container = app.current_container;
	        setContentView(R.layout.send_text);
	        input_box=(EditText) findViewById(R.id.editText1);
	        display_box=(TextView) findViewById(R.id.view1);
	 }
	 	// A function to take text input as a message and store it into the database
	    public void change_view(View v){
	   	String data=input_box.getText().toString();
	    	input_box.setText("");
	    	Log.d("XXXXX", data);
	    	//record_container.add_data(RecordContainer.DataType.LOCATION,get_loc_str());
	    	record_container.add_data(RecordContainer.DataType.TEXT, data);
	    	
	    	display_box.setText(data);
	    	    	
	   }
	    public void back(View v){
	    	this.finish();
	    }
	 


}
