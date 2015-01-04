package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
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
    public void onPushOpen(final Context context, final Intent intent) {
        Log.e("Push", "Clicked!!!!");
        try {
			if (intent == null)
			{
				System.out.println("Receiver intent null");
			}
			else
			{   
				final MediaPlayer mp = MediaPlayer.create(context, R.raw.ding2);
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
			}

		} catch (JSONException e) {
			Log.d("nice", "JSONException: " + e.getMessage());
		}
	}

	private void playSoundData(byte[] soundBytes, final Context context) {
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
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					Intent i = new Intent(context, MainActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
					context.getApplicationContext().startActivity(i);
				}
			});
			System.out.println("playing data sound");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}