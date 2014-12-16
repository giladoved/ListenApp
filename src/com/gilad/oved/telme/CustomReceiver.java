package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class CustomReceiver extends BroadcastReceiver {
private static final String TAG = "CustomReceiver";
 
String fromUsername;
String fromNickname;
byte[] data;

  @Override
  public void onReceive(final Context context, Intent intent) {
	  try {
			if (intent == null)
			{
				System.out.println("Receiver intent null");
			}
			else
			{   
				MediaPlayer mp = MediaPlayer.create(context, R.raw.ding);
			    mp.start();
				
				System.out.println("RECEIVED A MESSAGE!!");
				String action = intent.getAction();
				System.out.println("got action " + action);
				if (action.equals("com.gilad.oved.holdandtalk.PLAY_MESSAGE"))
				{
					String channel = intent.getExtras().getString("com.parse.Channel");
					JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

					Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
					Iterator itr = json.keys();
					while (itr.hasNext()) {
						String key = (String) itr.next();
						if (key.equals("username"))
						{
							Intent pupInt = new Intent(context, MainActivity.class);
							pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.getApplicationContext().startActivity(pupInt);
						}
						else if (key.equals("from")) {
						      fromUsername = json.getString("from");
						} else if (key.equals("fromName")) {
							fromNickname = json.getString("fromName");
						}
						Log.d(TAG, "..." + key + " => " + json.getString(key));
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
				            	System.out.println("foundVoice" + foundVoice);
				            	ParseFile f = (ParseFile)foundVoice.get("data");
				            	System.out.println("f is " + f);
				            	try {
									data = f.getData();
								    System.out.println("we made it " + data);
								    playSoundData(data, context);
								} catch (ParseException e1) {
									e1.printStackTrace();
								}
				            	
				            	//add to local file history too!
				            	File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + fromNickname + "," + fromUsername);
				            	Date createdAt = foundVoice.getCreatedAt();
							    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
							    String formattedDateString = formatter.format(createdAt); 
				            	File voiceNote = new File(dir, formattedDateString + ",sentflag.aac");
							    System.out.println("location of stirng will be: " + voiceNote.getAbsolutePath());
							    FileOutputStream fos;
							    try {
							        fos = new FileOutputStream(voiceNote);
							        fos.write(data);
							        fos.flush();
							        fos.close();
							    } catch (FileNotFoundException e1) {
							        // handle exception
							    } catch (IOException e1) {
							        // handle exception
							    }
							    
								Intent pupInt = new Intent(context, MainActivity.class);
								pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								context.getApplicationContext().startActivity(pupInt);

				            } else {
				                Log.d("Error", "Error: " + e.getMessage());
				            }
				        }
				    });
				}
			}

		} catch (JSONException e) {
			Log.d(TAG, "JSONException: " + e.getMessage());
		}
  }
  
  private void playSoundData(byte[] soundBytes, Context context) {
	    try {
	    	//http://stackoverflow.com/questions/1972027/android-playing-mp3-from-byte	
 	    	File tempAudio = File.createTempFile("tempSound", "3gp", context.getCacheDir());
	        tempAudio.deleteOnExit();
	        FileOutputStream fos = new FileOutputStream(tempAudio);
	        fos.write(soundBytes);
	        fos.close();
 
	        MediaPlayer mediaPlayer = new MediaPlayer();
	        FileInputStream fis = new FileInputStream(tempAudio);
	        mediaPlayer.setDataSource(fis.getFD());
	        mediaPlayer.setVolume(1.0f, 1.0f);

	        mediaPlayer.prepare();
	        mediaPlayer.start();
	        System.out.println("playing data sound");
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
}