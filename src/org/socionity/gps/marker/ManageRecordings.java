package org.socionity.gps.marker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;


public class ManageRecordings extends Activity {
	LinearLayout lv;
	ListView l;
	SocionityGPSApplication app;
	DbManager db_man;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (SocionityGPSApplication) getApplicationContext();
		db_man = app.db_man;
		lv = new LinearLayout(this);
		lv.setOrientation(LinearLayout.VERTICAL);
		l = new ListView(this);
		lv.addView(l);
		setContentView(lv);
		
		// --       
        //Register listview for context menu
        registerForContextMenu(l);
		//--        

	}

	@Override
	protected void onResume() {
		super.onResume();

        // Creating an ArrayAdapter of list of all the existing recordings
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1);
		for (RecordingEntry entry : db_man.get_recordings()) {

			String entry_str;
			entry_str = "-"+Long.toString(entry.id) + "- : " + entry.name;// + " (" + Long.toString(entry.time_start) + ")";
			entry_str += " " + Integer.toString(db_man.get_data(entry.id).size()) + " Datapoints recorded";
			adapter.add(entry_str);
		}
		l.setAdapter(adapter);
	}

    /* Event: Long-press on a recording entry */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
            super.onCreateContextMenu(menu, v, menuInfo);
            menu.setHeaderTitle("Select The Action"); 
            menu.add(0, v.getId(), 0, "Continue Recording"); 
            menu.add(0, v.getId(), 0, "Delete Recording");

    }
    
    @Override 
    public boolean onContextItemSelected(MenuItem item)
    { 
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
               
        //  info.position will give the index of selected item        		   
                   int IndexSelected = info.position;
                   String converted = (String) l.getItemAtPosition(IndexSelected);
                   String[] parsed = converted.split("-");
                   parsed[1] = parsed[1].trim();
                   final int rec_id = Integer.parseInt(parsed[1]);

                        // If "Continue Recording is selected, app will resume that specific recording"
                        if(item.getTitle()=="Continue Recording")
                        {
                        	db_man.set_last_record_id(rec_id);
                            // Code to execute when clicked on This Item
                        	Intent intent = new Intent(this, MainActivity.class);
                    		startActivity(intent);
                        	                        	                  
                        } 

                        // If "Delete Recording is selected, app will delete that specific recording"
                        else if(item.getTitle()=="Delete Recording")
                        {                           
                          // Code to execute when clicked on This Item
                        	AlertDialog.Builder adb = new AlertDialog.Builder(
            				ManageRecordings.this);
            				adb.setTitle("Warning");
            				adb.setMessage("Do you really want to delete recording with id = "
            				+ parsed[1]);
            				adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            					  public void onClick(DialogInterface dialog, int id) {
            						    if(db_man.delete_recording(rec_id)>0){
            						    	db_man.set_last_record_id(0);
            						    	finish();
            						    	startActivity(getIntent());
            						    }
            						  }
            						});
            				adb.setNegativeButton("No", null);
            				adb.show();
                        	
                        } 
                        else
                        {
                            return false;
                        } 
                        return true;               
      }  
	
		// --


}
