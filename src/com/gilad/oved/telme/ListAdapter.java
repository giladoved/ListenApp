package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ListAdapter extends ArrayAdapter<String> {

	private final Context context;
    private ArrayList<String> friendNicknames;
    private ArrayList<String> friendNumbers;
    private ArrayList<Bitmap> friendPictures;
    
    private boolean isRecording = false;
    private long pressedTime;
    
	  public ListAdapter(Context context, ArrayList<String> nicknames, ArrayList<String> numbers, ArrayList<Bitmap> pictures, ListView listView) {
		    super(context, R.layout.group_item, nicknames);  
		    this.context = context;
	        
	        friendNicknames = nicknames;
	        friendNumbers = numbers;
	        friendPictures = pictures;
	    }

	  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
		  if (convertView == null) {
	            LayoutInflater inf = (LayoutInflater) context
	                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
	            convertView = inf.inflate(R.layout.group_item, null);
	      }
	    
			final String numberTo = friendNumbers.get(position);
			final String nameTo = friendNicknames.get(position);
		  
			ImageView contactImage = (ImageView) convertView
					.findViewById(R.id.contactImage);
			contactImage.setImageBitmap(friendPictures.get(position));

			TextView contactName = (TextView) convertView
					.findViewById(R.id.contactNameLbl);
			contactName.setText(nameTo);

			TextView contactNumber = (TextView) convertView
					.findViewById(R.id.contactNumberLbl);
			contactNumber.setText(numberTo);
	        
	        final ImageButton messageBtn = (ImageButton) convertView
					.findViewById(R.id.contactBtn);
			messageBtn.setFocusable(false);
	        messageBtn.setOnTouchListener(new View.OnTouchListener() {

	            @Override
	            public boolean onTouch(View v, MotionEvent event) {
	            	CountDownTimer timeCounter;
	                switch(event.getAction()){
	                 case MotionEvent.ACTION_DOWN:
	                     Log.d(Constants.TAG, "Start Recording");
						if (!isRecording) {
							MainActivity.startRecording();
							isRecording = true;
							messageBtn.setImageResource(R.drawable.recordpressed);
				            pressedTime = System.currentTimeMillis();
						}
	                     break;
	                 case MotionEvent.ACTION_UP:
	                     Log.d(Constants.TAG, "Stop Recording");
	  					 messageBtn.setImageResource(R.drawable.record);

	                     //wait a second after releasing the button
	                     final Handler handler = new Handler();
	                     handler.postDelayed(new Runnable() {
	                       @Override
	                       public void run() {
	                    	   MainActivity.stopRecording();
	                           isRecording = false;
	           					long currentTime = System.currentTimeMillis();
	                           
	                           if (currentTime - pressedTime > 1000 + Constants.MINIMUM_RECORDING_LENGTH) { //messages must be at least 1 second long
	                        	   File audioFile = new File(MainActivity.recordingOutputFile);
	                 				 FileInputStream fileInputStream;
	                 				 final byte[] audioData = new byte[(int) audioFile.length()];

	                 				 try {
	                 				 	fileInputStream = new FileInputStream(audioFile);
	                 				 	fileInputStream.read(audioData);
	                 				 	fileInputStream.close();
	                 				 } catch (Exception e) {
	                 					Log.e(Constants.TAG, e.getLocalizedMessage());
	                 				 }

	                 				 final ParseObject voiceText = new ParseObject("messageData");
	                 				 voiceText.put("username", ParseUser.getCurrentUser().get("username"));
	                 				 ParseFile dataFile = new ParseFile(audioData);
	                 				 try {
	                 			 		dataFile.save();
	                 		 		} catch (ParseException e) {
	                 		 			Log.e(Constants.TAG, e.getLocalizedMessage());
	                  				}
	                  				voiceText.put("data", dataFile);
	                  				voiceText.saveInBackground(new SaveCallback() {

	                 					@Override
	                 					public void done(ParseException e) {
	                 						sendPush(numberTo, audioData, voiceText.getObjectId());
	                 						
	                 						//add to local history
	                 					    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + nameTo + "," + numberTo);
	                 					    Date createdAt = voiceText.getCreatedAt();
	                 					    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss"); //saves voice files in this format
	                 					    String formattedDateString = formatter.format(createdAt); 
	                 					    File voiceNote = new File(dir, formattedDateString + "," + Constants.SENT_FLAG + ".aac");
	                 					    FileOutputStream fos;
	                 					    try {
	                 					        fos = new FileOutputStream(voiceNote);
	                 					        fos.write(audioData);
	                 					        fos.flush();
	                 					        fos.close();
	                 					    } catch (FileNotFoundException e1) {
	                 					    	Log.e(Constants.TAG, e1.getLocalizedMessage());
	                 					    } catch (IOException e1) {
	                 					    	Log.e(Constants.TAG, e1.getLocalizedMessage());
	                 					    }
	                   					}
	                  				});
	                           } 
	                       }
	                     }, Constants.MILLISECONDS_AFTER_RELEASE);
	                     break;
	                }
	                return false;
	            }
	        });
	        
	        return convertView;
	    }
	  
	  public void sendMessage(String target, byte[] dataBytes) {
			try {
				ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
				query.whereEqualTo("username", target);
				ParsePush push = new ParsePush();
				push.setQuery(query);

				long dayInterval = 60 * 60 * 24; // 24 hrs expiration limit

				String base64Data = Base64.encodeToString(dataBytes,
						Base64.NO_PADDING);

				String dataStr = "{\"action\": \"com.gilad.oved.holdandtalk.PLAY_MESSAGE\",\"data\": \""
						+ base64Data
						+ "\",\"alert\": \"Message from "
						+ ParseUser.getCurrentUser().getUsername() + "\"}";
				JSONObject data = new JSONObject(dataStr);
				push.setData(data);
				push.setExpirationTimeInterval(dayInterval);
				push.sendInBackground();
			} catch (JSONException e) {
				Log.e(Constants.TAG, e.getLocalizedMessage());
			}
		}
	    
		public void sendPush(String target, byte[] dataBytes, String objID) {
			try {
				ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
				query.whereEqualTo("username", target);
				ParsePush push = new ParsePush();
				push.setQuery(query);

				long dayInterval = 60 * 60 * 24; // 24 hrs
				String dataStr = "{\"action\": \"com.gilad.oved.holdandtalk.PLAY_MESSAGE\",\"from\":\""
						+ ParseUser.getCurrentUser().getUsername()
						+ "\",\"from_name\":\""
						+ ParseUser.getCurrentUser().get("nickname") 
						+ "\",\"message_id\":\"" + objID  
						+ "\",\"alert\": \"Message from "
						+ ParseUser.getCurrentUser().getUsername() + "\"}";
				JSONObject data = new JSONObject(dataStr);
				push.setData(data);
				push.setExpirationTimeInterval(dayInterval);
				push.sendInBackground();
			} catch (JSONException e) {
				Log.e(Constants.TAG, e.getLocalizedMessage());
			}
		}
}
