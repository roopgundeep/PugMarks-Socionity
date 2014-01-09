package org.socionity.gps.marker;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AudioCapture extends Activity {
	DbManager db_man;
	SocionityGPSApplication app;
	RecordContainer record_container;
	Button record_button;
	Button play_button;
	public boolean RecordState = true;
	public boolean PlayerState = true;
	public MediaRecorder mRecorder;
	public MediaPlayer mPlayer;
	public String mfileName = null;
	public String LOG_TAG = "MainActivity";
	String audioName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (SocionityGPSApplication) getApplicationContext();
		db_man = app.db_man;
		record_container = app.current_container;
		setContentView(R.layout.activity_audio);
		//Creating Buttons
		record_button = (Button) findViewById(R.id.audio_record_button);
		play_button = (Button) findViewById(R.id.audio_play_button);
	}
	//Start or Stop recording
	private void onRecord(boolean start) {
		if (start) {
			record_start();
		} else {
			record_stop();
		}
	}
	//Start the player
	private void onPlay(boolean start) {
		if (start) {
			player_start();
		} else {
			player_stop();
		}
	}
	//intial start of the player
	private void player_start() {
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mfileName);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(LOG_TAG, "prepare() failed");
		}
	}
	//to stop the player
	private void player_stop() {
		mPlayer.release();
		mPlayer = null;
	}
	//when player starts...start the recording
	private void record_start() {
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		File audioFolder = DbManager.app_directory;
		audioFolder.mkdirs();
		audioName = "/audio" + Long.toString(db_man.get_next_id()) + ".3gp";
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
	//stop the recording
	private void record_stop() {
		record_container.add_data(RecordContainer.DataType.AUDIO, audioName);
		mRecorder.stop();
		mRecorder.release();
		mRecorder = null;
	}
	//recording of audio
	public void audio_record(View v) {
		onRecord(RecordState);
		if (RecordState) {
			record_button.setText("Stop Recording");
			play_button.setEnabled(false);
		} 
		else {
			play_button.setEnabled(true);
			record_button.setText("Start Recording");
		}
		RecordState = !RecordState;
	}
	//Want to play the last recorded audio
	public void play_last_record(View v) {
		onPlay(PlayerState);
		if (PlayerState) {
			play_button.setText("Stop");
			record_button.setEnabled(false);
		} else {
			play_button.setText("Play Last Record");
			record_button.setEnabled(true);
		}
		PlayerState = !PlayerState;
	}

	public void close(View v) {
		this.finish();
	}
	/*
	 * private void onRecord(boolean start){ if(start) { record_start(); } else
	 * { record_stop(); } } private void onPlay(boolean start){ if(start){
	 * player_start(); } else{ player_stop(); } } private void player_start(){
	 * mPlayer = new MediaPlayer(); try { mPlayer.setDataSource(mfileName);
	 * mPlayer.prepare(); mPlayer.start(); } catch (IOException e) {
	 * Log.e(LOG_TAG, "prepare() failed"); } } private void player_stop(){
	 * mPlayer.release(); mPlayer = null; } private void record_start(){
	 * mRecorder = new MediaRecorder();
	 * mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	 * mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	 * mRecorder.setOutputFile(mfileName);
	 * mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	 * 
	 * try { mRecorder.prepare(); } catch (IOException e) { Log.e(LOG_TAG,
	 * "prepare() failed"); } mRecorder.start();
	 * 
	 * } private void record_stop(){ mRecorder.stop(); mRecorder.release();
	 * mRecorder = null; } public void audio_record(View v){
	 * onRecord(RecordState); if(RecordState) {
	 * record_button.setText("Stop Recording"); play_button.setEnabled(false);
	 * }else{ record_button.setText("Start Recording");
	 * play_button.setEnabled(true); } RecordState=!RecordState; } public void
	 * play_last_record(View v){ onPlay(PlayerState); if(PlayerState) {
	 * play_button.setText("Stop"); record_button.setEnabled(false); } else{
	 * play_button.setText("Play Last Record"); record_button.setEnabled(true);
	 * } PlayerState=!PlayerState; }
	 */

}
