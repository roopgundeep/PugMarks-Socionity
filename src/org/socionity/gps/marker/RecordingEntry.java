package org.socionity.gps.marker;

public class RecordingEntry implements Comparable<RecordingEntry> {
	public long id;
	public long time_start;
	public long single_entry;
	public String name;
	public String type;

	public RecordingEntry() {
		name = new String();
	}

	public RecordingEntry(long id, long time_start, String name, String type, long single_entry) {
		this.id = id;
		this.time_start = time_start;
		this.name = name;
		this.type = type;
		this.single_entry=single_entry;
	}
	public RecordingEntry(String name, String type, long single_entry) {
		this.name = name;
		this.type = type;
		this.single_entry=single_entry;
	}
	public long fetchSingle(){
		return this.single_entry;
	}
	@Override
	public int compareTo(RecordingEntry another) {
		if (this.time_start < another.time_start)
			return -1;
		else if (this.time_start == another.time_start)
			return 0;
		else
			return 1;
	}

}
