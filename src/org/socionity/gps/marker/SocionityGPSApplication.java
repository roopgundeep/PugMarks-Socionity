package org.socionity.gps.marker;

import android.app.Application;

public class SocionityGPSApplication extends Application {
	public DbManager db_man;
	public long current_record_id;
	public RecordContainer current_container;

	@Override
	public void onCreate() {
		super.onCreate();
		db_man = new DbManager(getApplicationContext());
		db_man.open();
	}

	public void set_current_container(RecordContainer rec) {
		current_container = rec;
	}

	/**
	 * Call this function to completely purge the database and restore state of
	 * application to a new start
	 */
	public void purge_db() {
		current_container = null;
		current_record_id = 0;
		db_man.purge_db();
	}

}
