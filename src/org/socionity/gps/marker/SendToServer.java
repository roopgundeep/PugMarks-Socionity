package org.socionity.gps.marker;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class SendToServer extends Activity {
	ProgressBar progress_bar;
	Button send_button;
	Button send_local;
	SocionityGPSApplication app;
	DbManager db_man;
	SendTask task;
	TextView error;
	EditText username_input;
	EditText password_input;
	String button_text = "";
	String u_name;
	String password;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.send_to_server);
		error = (TextView) findViewById(R.id.error_box);
		send_button = (Button) findViewById(R.id.SendStatus);
		send_local = (Button) findViewById(R.id.SendLocal);
		progress_bar = (ProgressBar) findViewById(R.id.progressBar1);
		app = (SocionityGPSApplication) getApplicationContext();
		db_man = app.db_man;
		username_input = (EditText) findViewById(R.id.username);
		password_input = (EditText) findViewById(R.id.password);
		System.out.println("checking");
		/*
		 * sUsername=username_input.getText().toString();
		 * sPassword=password_input.getText().toString();
		 * if(sUsername.matches("")){ send_button.setEnabled(false);
		 * send_button.setText("No username"); }
		 */

		if (db_man.get_recordings().size() == 0) {
			button_text += getResources().getString(R.string.no_data_to_send);
			// send_button.setEnabled(false);
			// send_button.setText(getResources().getString(
			// R.string.no_data_to_send));
		}
		if (!button_text.matches("")) {
			send_button.setEnabled(false);
			send_local.setEnabled(false);
			send_button.setText(button_text);
			error.setText(button_text);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.finish();
	}

	/* 
	 * If one want to locally store the data, a folder "PugMarks_local" is created
	 * upload.zip file will be created in that folder
	 * upload.zip file contains an 'index' file and the multimedia attachments
	 * 'index' file contains a JSON format of all the GPS points with each one's attributes
	 * Use send_local() as a direct black-box function if needed
	 */
	public void send_local(View v) {
		Resources res = getResources();
		// publishProgress(s_packing, "0");
		File output_file1 = new File(Environment.getExternalStorageDirectory()
				+ "/PugMarks_local");
		if (!output_file1.exists()) {
			if (output_file1.mkdir()) {
				// directory is created;
			}
		}
		//final String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		File output_file = new File(output_file1, "upload.zip");
		ZipOutputStream zos;
		try {

			zos = new ZipOutputStream(new BufferedOutputStream(
					new FileOutputStream(output_file)));

			// Pack JSON data
			JSONArray data = new JSONArray(); // An array , with each
												// element
												// one recording
			for (RecordingEntry rec : db_man.get_recordings()) {
				JSONObject ob = new JSONObject(); // The recording object
				try {
					ob.put("name", rec.name); // Name of recording
					ob.put("time", rec.time_start); // Time at which
													// recording
													// was started
					ob.put("type", rec.type);
					JSONArray rec_data = new JSONArray(); // The recording
															// data
															// points array;
					for (RecordingDataEntry rec_dat : db_man.get_data(rec.id)) {
						JSONObject dat_ob = new JSONObject(); // A single
																// data
																// point
						dat_ob.put("time", rec_dat.time);
						dat_ob.put("data_type", rec_dat.data_type);
						if (rec_dat.data_type.charAt(0) == 'P'
								|| rec_dat.data_type.charAt(0) == 'V'
								|| rec_dat.data_type.charAt(0) == 'A') {
							// File upload types
							try {
								Log.d("json", "zipping " + rec_dat.data);
								File f = new File(DbManager.app_directory,
										rec_dat.data);
								if (!f.exists() || !f.canRead()) {
									Log.d("send2server",
											"Error ! Cannot read file : "
													+ f.toString());
								}
								ZipEntry entry = new ZipEntry(rec_dat.data);
								zos.putNextEntry(entry);
								FileInputStream fin = new FileInputStream(f);
								byte[] buf = new byte[2048];
								while (true) {
									int size = fin.read(buf);
									if (size == -1)
										break;
									zos.write(buf, 0, size);
								}
								fin.close();
								zos.closeEntry();
							} catch (Exception e) {
								Log.d("send2server",
										"Caught Exception " + e.toString());
							}
						}
						dat_ob.put("data", rec_dat.data);
						rec_data.put(dat_ob);
					}
					ob.put("data", rec_data);
				} catch (JSONException e) {
					Log.d("send2server", "Caught JSON Exception!");
				}
				data.put(ob);
			}
			zos.putNextEntry(new ZipEntry("index"));
			zos.write(data.toString().getBytes());
			zos.closeEntry();
			zos.close();
			// publishProgress(s_connecting, "25");
			Log.d("send", "Data packed");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch
			// blocksrc/org/socionity/gps/marker/HomePage.java
			e1.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(this, "Data packed locally" , Toast.LENGTH_SHORT).show();
	}

	/* Sending user credentials: username and password */
	public void send_data(View v) {
		u_name = username_input.getText().toString();
		password = password_input.getText().toString();

		if (u_name.matches("")) {
			button_text += "Username missing ";
			// send_button.setEnabled(false);
			// send_button.setText("Username needed");
		}
		task = new SendTask();
		task.execute();
	}

	/*
	 * Uploading the projects onto the Socionity Server 
	 * upload.zip file is created automatically with 'index' file and multimedia attachments
	 * then this upload.zip file is uploaded to the database at Socionity Server with user credentials
	 */
	private class SendTask extends AsyncTask<Context, String, Boolean> {
		final static String BASE = "http://socionity.iiit.ac.in/gpsapp/gpsback";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			send_button.setEnabled(false);
		}

		@Override
		protected Boolean doInBackground(Context... params) {
			Resources res = getResources();
			String s_packing = res.getString(R.string.packaging_data);
			String s_connecting = res.getString(R.string.connecting_to_server);
			String s_sending = res.getString(R.string.sending_data);
			String s_done = res.getString(R.string.done);
			publishProgress(s_packing, "0");
			File output_file = new File(DbManager.app_directory, "upload.zip");
			ZipOutputStream zos;
			try {
				zos = new ZipOutputStream(new BufferedOutputStream(
						new FileOutputStream(output_file)));

				// Pack JSON data
				JSONArray data = new JSONArray(); // An array , with each
													// element
													// one recording
				for (RecordingEntry rec : db_man.get_recordings()) {
					JSONObject ob = new JSONObject(); // The recording object
					try {
						ob.put("name", rec.name); // Name of recording
						ob.put("time", rec.time_start); // Time at which
														// recording
														// was started
						ob.put("type", rec.type);
						JSONArray rec_data = new JSONArray(); // The recording
																// data
																// points array;
						for (RecordingDataEntry rec_dat : db_man
								.get_data(rec.id)) {
							JSONObject dat_ob = new JSONObject(); // A single
																	// data
																	// point
							dat_ob.put("time", rec_dat.time);
							dat_ob.put("data_type", rec_dat.data_type);
							if (rec_dat.data_type.charAt(0) == 'P'
									|| rec_dat.data_type.charAt(0) == 'V'
									|| rec_dat.data_type.charAt(0) == 'A') {
								// File upload types
								try {
									Log.d("json", "zipping " + rec_dat.data);
									File f = new File(DbManager.app_directory,
											rec_dat.data);
									if (!f.exists() || !f.canRead()) {
										Log.d("send2server",
												"Error ! Cannot read file : "
														+ f.toString());
									}
									ZipEntry entry = new ZipEntry(rec_dat.data);
									zos.putNextEntry(entry);
									FileInputStream fin = new FileInputStream(f);
									byte[] buf = new byte[2048];
									while (true) {
										int size = fin.read(buf);
										if (size == -1)
											break;
										zos.write(buf, 0, size);
									}
									fin.close();
									zos.closeEntry();
								} catch (Exception e) {
									Log.d("send2server", "Caught Exception "
											+ e.toString());
								}
							}
							dat_ob.put("data", rec_dat.data);
							rec_data.put(dat_ob);
						}
						ob.put("data", rec_data);
					} catch (JSONException e) {
						Log.d("send2server", "Caught JSON Exception!");
					}
					data.put(ob);
				}
				zos.putNextEntry(new ZipEntry("index"));
				zos.write(data.toString().getBytes());
				zos.closeEntry();
				zos.close();
				publishProgress(s_connecting, "25");
				Log.d("send", "Data packed");
				// -- till this point, upload.zip has been created
				// -- now this file is to be uploaded onto the server
				try {
					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							10000);
					HttpConnectionParams.setSoTimeout(httpParameters, 10000);
					HttpClient httpclient = new DefaultHttpClient(
							httpParameters);
					// store.php on Socionity server unzips the file
					// and parses it to store projects in the database
					HttpPost httppost = new HttpPost(BASE + "/store.php");
					MultipartEntity form_entity = new MultipartEntity();
					form_entity
							.addPart("data", new StringBody(data.toString()));
					Log.d("XXXX", data.toString());
					form_entity.addPart("username", new StringBody(u_name));
					form_entity.addPart("password", new StringBody(password));
					form_entity.addPart("file", new FileBody(output_file));
					httppost.setEntity(form_entity);
					Log.d("send", "Conecting to " + BASE + "/store.php");
					HttpResponse resp = httpclient.execute(httppost);
					publishProgress(s_sending, "50");
					HttpEntity entity = resp.getEntity();
					String ret = readStream(entity.getContent());
					if (ret.equals("1")) {
						return true;
					} else if (ret.equals("2")) {
						button_text += "Wrong credentials";
						return false;
					} else if (ret.equals("0")) {
						button_text += "Error";
						return false;
					}
				} catch (Exception e) {
					Log.d("send", "GOt exception " + e.toString());
					return false;
				}
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch
				// blocksrc/org/socionity/gps/marker/HomePage.java
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				send_button.setText(getResources().getString(R.string.done));
				progress_bar.setProgress(100);
				app.purge_db();
			} else {
				send_button.setText(button_text);
				// send_button.setText(getResources().getString(
				// R.string.sending_failed_try_again));
				button_text = "";
				send_button.setEnabled(true);
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			send_button.setText(values[0]);
			Integer p = Integer.parseInt(values[1]);
			progress_bar.setProgress(p);
			if (p == 0) {
				progress_bar.setIndeterminate(true);
			} else {
				progress_bar.setIndeterminate(false);
			}
		}

		private String readStream(InputStream in) {
			BufferedReader reader = null;
			String ret = "";
			try {
				reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				while ((line = reader.readLine()) != null) {
					ret += line;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return ret;
		}

	}
}
