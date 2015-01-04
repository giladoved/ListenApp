package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Group> groups;
    
	private ListView historyList;
		
    private ArrayList<String> friendNicknames;
    private ArrayList<String> friendNumbers;
    private ArrayList<Bitmap> friendPictures;
    
    private boolean isRecording = false;
    private long pressedTime;
    
    public ExpandableListAdapter(Context context, ArrayList<Group> groups, ArrayList<String> nicknames, ArrayList<String> numbers, ArrayList<Bitmap> pictures, ExpandableListView listView) {
        this.context = context;
        this.groups = groups;
        
        friendNicknames = nicknames;
        friendNumbers = numbers;
        friendPictures = pictures;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<Child> chList = groups.get(groupPosition).getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        final Child child = (Child) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_item, null);
        }
        
		Button removeContactBtn = (Button) convertView
				.findViewById(R.id.removeContactBtn);
		removeContactBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
        		//remove local user file
			    File userFile = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + friendNicknames.get(groupPosition) + "," + friendNumbers.get(groupPosition));
        	    String[] children = userFile.list();
        	    //have to delete all contents of folder before deleting folder
        	    for (int i = 0; i < children.length; i++) {
        	    	new File(userFile, children[i]).delete();
        	    }
			    boolean deleted = userFile.delete();
			    
			    //remove local user profile picture
			    File userPicFile = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/Pictures/" + friendNicknames.get(groupPosition) + "," + friendNumbers.get(groupPosition) + ".jpg");
			    boolean pictureDeleted = userPicFile.delete();
        		if (deleted && pictureDeleted) {
        			friendNicknames.remove(groupPosition);
			   		friendNumbers.remove(groupPosition);
			   		friendPictures.remove(groupPosition);
					groups.remove(groupPosition);
					notifyDataSetChanged();
					
					Toast.makeText(context, "Contact Deleted", Toast.LENGTH_SHORT).show();
        		}
			}
		});
		
		Button clearHistoryBtn = (Button) convertView
				.findViewById(R.id.clearHistoryBtn);
		clearHistoryBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(context, "History Cleared", Toast.LENGTH_SHORT).show();
				File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + friendNicknames.get(groupPosition) + "," + friendNumbers.get(groupPosition));
			    if (dir.listFiles() != null) {
			    	for (File f : dir.listFiles()) {
			    		if (!f.getName().contains("jpg")) //delete everything except the profile picture
			    			f.delete();
			    	}
			    }
			    child.setDates(new ArrayList<String>());
			    child.setPaths(new ArrayList<String>());
			    child.setSentBools(new ArrayList<String>());
				notifyDataSetChanged();
			}
		});
        		
		historyList = (ListView) convertView.findViewById(R.id.historyListView);
		final SimpleArrayAdapter adapter = new SimpleArrayAdapter(context, R.layout.simplest_list_item_1, child.getDates());
		historyList.setAdapter(adapter);
		historyList.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	//fixes nested scrolling bug
		        v.getParent().requestDisallowInterceptTouchEvent(true);
		        return false;
		    }
		});
		historyList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long arg3) {
				File file = new File(child.getPaths().get(pos)); // acquire the file from path string
				final MediaPlayer mediaPlayer = new MediaPlayer();
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				try {
					mediaPlayer.setDataSource(file.getAbsolutePath());
					mediaPlayer.prepare();
					mediaPlayer.start();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}			
		}); 

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Child> chList = groups.get(groupPosition).getItems();
        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        final Group group = (Group) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater) context
                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
                convertView = inf.inflate(R.layout.group_item, null);
        }

		ImageView contactImage = (ImageView) convertView
				.findViewById(R.id.contactImage);
		contactImage.setImageBitmap(group.getPicture());

		TextView contactName = (TextView) convertView
				.findViewById(R.id.contactNameLbl);
		contactName.setText(group.getName());

		TextView contactNumber = (TextView) convertView
				.findViewById(R.id.contactNumberLbl);
		contactNumber.setText(group.getNumber());
		
		final String numberTo = group.getNumber();
		final String nameTo = group.getName();
        
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
                 					    
                 					    ArrayList<String> paths = group.getItems().get(0).getPaths();
                 					    ArrayList<String> sentBools = group.getItems().get(0).getSentBools();
                 					    ArrayList<String> dates = group.getItems().get(0).getDates();
                 					    //add to the front (order n...) to keep newer messages on top
                 					    paths.add(0,voiceNote.getAbsolutePath());
                 					    sentBools.add(0,"sent");
                 					    
                 					    Date dte = voiceText.getCreatedAt();
                 					    SimpleDateFormat formatter2 = new SimpleDateFormat("HH:mm:ss MMM d");
                 					    String formattedDateString2 = formatter2.format(dte); 
                 						dates.add(0, formattedDateString2);
                 					    
                 					    group.getItems().get(0).setDates(dates);
                 					    group.getItems().get(0).setPaths(paths);
                 					    group.getItems().get(0).setSentBools(sentBools);
                 					    notifyDataSetChanged();
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

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    
    
    private class SimpleArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public SimpleArrayAdapter(Context context, int textViewResourceId,
            List<String> objects) {
          super(context, textViewResourceId, objects);
          for (int i = 0; i < objects.size(); ++i) {
            mIdMap.put(objects.get(i), i);
          }
        }

        @Override
        public long getItemId(int position) {
          String item = getItem(position);
          return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
          return true;
        }

      }


}