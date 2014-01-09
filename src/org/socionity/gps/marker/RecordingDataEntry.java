package org.socionity.gps.marker;


public class RecordingDataEntry implements Comparable<RecordingDataEntry> {
	long data_id;
	long record_id;
	long time;
	String data_type;
	String data;
	public RecordingDataEntry(){
	}
	public RecordingDataEntry(long id, long rec_id, long time, String data_type, String data) {
		this.data_id = id;
		this.record_id = rec_id;
		this.time = time;
		this.data_type = data_type;
		this.data = data;
	}
	@Override
	public int compareTo(RecordingDataEntry another) {
		if (this.time < another.time)
			return -1;
		else if (this.time == another.time)
			return 0;
		else
			return 1;
	}
}
