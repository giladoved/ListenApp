package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class CustomReceiver extends BroadcastReceiver {
 
	private String fromUsername;
	private String fromNickname;
	private byte[] data;

  @Override
  public void onReceive(final Context context, Intent intent) {
	  try {
			if (intent != null) { 
				final MediaPlayer mp = MediaPlayer.create(context, R.raw.ding2);
			    mp.start();
				
				String action = intent.getAction();
				if (action.equals("com.gilad.oved.holdandtalk.PLAY_MESSAGE"))
				{
					JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

					Iterator itr = json.keys();
					while (itr.hasNext()) {
						String key = (String) itr.next();
						if (key.equals("from")) {
						      fromUsername = json.getString("from");
						} else if (key.equals("from_name")) {
							fromNickname = json.getString("from_name");
						}
						Log.d(Constants.TAG, "..." + key + " => " + json.getString(key));
					}
					
					ParseQuery<ParseObject> query = ParseQuery.getQuery("messageData");
				    query.whereEqualTo("username", fromUsername);
				    query.addDescendingOrder("createdAt");
				    query.setLimit(1);
				    query.findInBackground(new FindCallback<ParseObject>() {
				        @Override
						public void done(List<ParseObject> results, ParseException e) {
				            if (e == null) {
				            	ParseObject foundVoice = results.get(0);
				            	ParseFile f = (ParseFile)foundVoice.get("data");
				            	try {
					            	//add to local file history too!
				            		data = f.getData();								    
					            	File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + fromNickname + "," + fromUsername);
					            	Date createdAt = foundVoice.getCreatedAt();
								    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
								    String formattedDateString = formatter.format(createdAt); 
					            	File voiceNote = new File(dir, formattedDateString + "," + Constants.RECEIVED_FLAG + ".aac"); 
								    FileOutputStream fos;
								    try {
								        fos = new FileOutputStream(voiceNote);
								        fos.write(data);
								        fos.flush();
								        fos.close();
								    } catch (FileNotFoundException e1) {
								    	Log.e(Constants.TAG, e1.getLocalizedMessage());
								    } catch (IOException e1) {
								    	Log.e(Constants.TAG, e1.getLocalizedMessage());
								    }
								    
									mp.setOnCompletionListener(new OnCompletionListener() {
										
										@Override
										public void onCompletion(MediaPlayer mp) {
											final Handler handler = new Handler();
											handler.postDelayed(new Runnable() {
											  @Override
											  public void run() {
												   playSoundData(data, context);
											  }
											}, 200);
										}
									});
								} catch (ParseException e1) {
									Log.e(Constants.TAG, e1.getLocalizedMessage());
								}
				            	
				            } else {
				                Log.e(Constants.TAG, e.getMessage());
				            }
				        }
				    });
				}
			}

		} catch (JSONException e) {
			Log.e(Constants.TAG, e.getMessage());
		}
  }
  
  private void playSoundData(byte[] soundBytes, final Context context) {
	    try {
	    	//http://stackoverflow.com/questions/1972027/android-playing-mp3-from-byte	
 	    	File tempAudio = File.createTempFile("tempSound", "3gp", context.getCacheDir());
	        tempAudio.deleteOnExit();
	        FileOutputStream fos = new FileOutputStream(tempAudio);
	        fos.write(soundBytes);
	        fos.close();
 
	        MediaPlayer mediaPlayer = new MediaPlayer();
	        FileInputStream fis = new FileInputStream(tempAudio);
	        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	        mediaPlayer.setDataSource(fis.getFD());
	        mediaPlayer.prepare();
	        mediaPlayer.start();
	        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					Intent pupInt = new Intent(context, MainActivity.class);
					pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.getApplicationContext().startActivity(pupInt);
				}
			});
	        Log.v(Constants.TAG, "playing data sound");
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
}