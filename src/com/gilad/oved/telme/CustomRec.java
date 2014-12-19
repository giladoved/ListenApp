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

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Environment;
import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseQuery;

public class CustomRec extends ParsePushBroadcastReceiver {

	String fromUsername;
	String fromNickname;
	String messageId;
	byte[] data;
	
	@Override
	public void onPushReceive(final Context context, Intent intent) {
		System.out.println("intent: " + intent);
		try {
			if (intent == null)
			{
				System.out.println("Receiver intent null");
			}
			else 
			{   
				final MediaPlayer mp = MediaPlayer.create(context, R.raw.ding2);
			    mp.start();

				String action = intent.getAction();
				System.out.println("got action " + action);
				if (action.equals("com.gilad.oved.holdandtalk.PLAY_MESSAGE"))
				{
					String channel = intent.getExtras().getString("com.parse.Channel");
					JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

					Log.d("nice", "got action " + action + " on channel " + channel + " with:");
					Iterator itr = json.keys();
					System.out.println("working with this: " + json);
					while (itr.hasNext()) {
						String key = (String) itr.next();
						System.out.println("has key: " + key);
						if (key.equals("from")) {
						      fromUsername = json.getString("from");
						} else if (key.equals("fromName")) {
							fromNickname = json.getString("fromName");
						}
						Log.d("nice", "..." + key + " => " + json.getString(key));
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
										System.out.println("1parse playing exception: " + e1);
								    } catch (IOException e1) {
								        // handle exception
										System.out.println("2parse playing exception: " + e1);
								    }
								    
								    System.out.println("showing new intent now");
									Intent pupInt = new Intent(context, MainActivity.class);
									pupInt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									context.getApplicationContext().startActivity(pupInt);
								    
									mp.setOnCompletionListener(new OnCompletionListener() {

										   public void onCompletion(MediaPlayer mp) {
											    playSoundData(data, context);
										    }
										});
								} catch (ParseException e1) {
									System.out.println("parse playing exception: " + e1);
								}
				            	
				            } else {
				                Log.d("Error", "Error: " + e.getMessage());
				            }
				        }
				    });
				}
			}

		} catch (JSONException e) {
			Log.d("nice", "JSONException: " + e.getMessage());
		}
	}
	
	@Override
    public void onPushOpen(final Context context, final Intent intent) {
        Log.e("Push", "Clicked!!!!");
        try {
			if (intent == null)
			{
				System.out.println("Receiver intent null");
			}
			else
			{   
				MediaPlayer mp = MediaPlayer.create(context, R.raw.ding2);
			    mp.start();
				
				System.out.println("RECEIVED A MESSAGE!!");
				String action = intent.getAction();
				System.out.println("got action " + action);
				String channel = intent.getExtras().getString("com.parse.Channel");
				JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));

				Log.d("nice", "got action " + action + " on channel " + channel + " with:");
				Iterator itr = json.keys();
				while (itr.hasNext()) {
					String key = (String) itr.next();
					if (key.equals("from")) {
						fromUsername = json.getString("from");
					} else if (key.equals("idid")) {
						messageId = json.getString("idid");
					}
					Log.d("nice", "..." + key + " = > " + json.getString(key));
				}

				if (fromUsername != null && fromUsername.length() > 0) {
					System.out.println("username!!! exists ----> " + fromUsername);
					System.out.println("message id is : " + messageId);
					ParseQuery<ParseObject> query = ParseQuery
							.getQuery("messageData");
					query.whereEqualTo("username", fromUsername);
				    query.whereEqualTo("objectId", messageId);
					query.addDescendingOrder("createdAt");
					query.setLimit(1);
					query.findInBackground(new FindCallback<ParseObject>() {
						@Override
						public void done(List<ParseObject> results,
								ParseException e) {
							if (e == null) {
								ParseObject foundVoice = results.get(0);
								System.out.println("foundVoice" + foundVoice);
								ParseFile f = (ParseFile) foundVoice
										.get("data");
								System.out.println("f is " + f);
								try {
									data = f.getData();
									System.out.println("we made it " + data);
									playSoundData(data, context);
									
									Intent i = new Intent(context, MainActivity.class);
									i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
									context.getApplicationContext().startActivity(i);
								} catch (ParseException e1) {
									e1.printStackTrace();
								}
							} else {
								Log.d("Error", "Error: " + e.getMessage());
							}
						}
					});
				} else  {
					System.out.println("username does not exists ----> " + action);
				}
				//}
			}

		} catch (JSONException e) {
			Log.d("nice", "JSONException: " + e.getMessage());
		}
	}

	private void playSoundData(byte[] soundBytes, Context context) {
		try {
			// http://stackoverflow.com/questions/1972027/android-playing-mp3-from-byte
			File tempAudio = File.createTempFile("tempSound", "3gp",
					context.getCacheDir());
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