package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Group> groups;
    
	boolean firstTouch = true;
	ListView historyList;
		
    ArrayList<String> friendNicknames;
    ArrayList<String> friendNumbers;

    public ExpandableListAdapter(Context context, ArrayList<Group> groups, ArrayList<String> nicknames, ArrayList<String> numbers, ExpandableListView listView) {
        this.context = context;
        this.groups = groups;
        
        friendNicknames = nicknames;
        friendNumbers = numbers;
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

        Child child = (Child) getChild(groupPosition, childPosition);
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
			   	/*ParseQuery<ParseUser> query = ParseUser.getQuery();
        		query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
        			public void done(ParseUser foundUser, ParseException e) {
        				if (e == null) {
        					foundUser.put("friendsNames", friendNicknames);
        					foundUser.put("friendsNumbers", friendNumbers);
        					foundUser.saveInBackground();
        					
        					groups.remove(groupPosition);
        					notifyDataSetChanged();
        				}
        			}
        		});*/
        		
        		//remove local user file
			    File userFile = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + friendNicknames.get(groupPosition) + "," + friendNumbers.get(groupPosition));
        		System.out.println("userfile dir is : "+ userFile.getAbsolutePath());
        	    String[] children = userFile.list();
        	    for (int i = 0; i < children.length; i++) {
        	    	new File(userFile, children[i]).delete();
        	    }
			    boolean deleted = userFile.delete();
        		if (deleted) {
        			friendNicknames.remove(groupPosition);
			   		friendNumbers.remove(groupPosition);
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
				Toast.makeText(context, "Clear History!!!", Toast.LENGTH_SHORT).show();
				//delete local history
				File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + friendNicknames.get(groupPosition) + "," + friendNumbers.get(groupPosition));
			    if (dir.listFiles() != null) {
			    	for (File f : dir.listFiles()) {
			    		if (!f.getName().contains("jpg"))
			    			f.delete();
			    	}
			    }
			}
		});
        
		System.out.println("child is :" + child);
		
		historyList = (ListView) convertView.findViewById(R.id.historyListView);
		final SimpleArrayAdapter adapter = new SimpleArrayAdapter(context, android.R.layout.simple_list_item_1, child.getList());
		historyList.setAdapter(adapter);
		historyList.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        // disallow the onTouch for your scrollable parent view 
		        v.getParent().requestDisallowInterceptTouchEvent(true);
		        return false;
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
        Group group = (Group) getGroup(groupPosition);
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
        
        ImageButton messageBtn = (ImageButton) convertView
				.findViewById(R.id.contactBtn);
        messageBtn.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (firstTouch) {
					MainActivity.start();
				}
				firstTouch = false;
				
				return false;
			}

		});
        messageBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				System.out.println("touch up......");
				System.out.println("walkie talkie noise goes here!!! : " + nameTo);
				MainActivity.stop();
				firstTouch = true;
				
				File audioFile = new File(MainActivity.outputFile);
				FileInputStream fileInputStream;
				final byte[] audioData = new byte[(int) audioFile.length()];

				try {
					fileInputStream = new FileInputStream(audioFile);
					fileInputStream.read(audioData);
					fileInputStream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				final ParseObject voiceText = new ParseObject("messageData");
				voiceText.put("username", ParseUser.getCurrentUser().get("username"));
				ParseFile dataFile = new ParseFile(audioData);
				try {
					dataFile.save();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				voiceText.put("data", dataFile);
				voiceText.saveInBackground(new SaveCallback() {

					@Override
					public void done(ParseException e) {
						System.out.println("walkie talkie noise goes here!!!"
								+ nameTo);
						sendPush(numberTo, audioData, voiceText.getObjectId());
						
						//add to local history
					    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + nameTo + "," + numberTo);
					    File voiceNote = new File(dir, String.valueOf(voiceText.getCreatedAt()) + ",sentflag.aac");
					    FileOutputStream fos;
					    try {
					        fos = new FileOutputStream(voiceNote);
					        fos.write(audioData);
					        fos.flush();
					        fos.close();
					    } catch (FileNotFoundException e1) {
					        // handle exception
					    } catch (IOException e1) {
					        // handle exception
					    }
					}
				});
				
				firstTouch = true;
			}
		});
		messageBtn.setFocusable(false);
        
        return convertView;
    }

    public void sendMessage(String target, byte[] dataBytes) {
		try {
			System.out.println("audioDATA: " + dataBytes.length);
			ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
			query.whereEqualTo("username", target);
			ParsePush push = new ParsePush();
			push.setQuery(query);

			long dayInterval = 60 * 60 * 24; // 24 hrs

			String base64Data = Base64.encodeToString(dataBytes,
					Base64.NO_PADDING);

			String dataStr = "{\"action\": \"com.gilad.oved.holdandtalk.PLAY_MESSAGE\",\"data\": \""
					+ base64Data
					+ "\",\"alert\": \"Message from "
					+ ParseUser.getCurrentUser().getUsername() + "\"}";
			System.out.println("data----" + dataStr);
			JSONObject data = new JSONObject(dataStr);
			push.setData(data);
			push.setExpirationTimeInterval(dayInterval);
			push.sendInBackground(new SendCallback() {

				@Override
				public void done(ParseException e) {
					System.out.println("play walkie talkie sound - " + e);
					// play walkie talkie sound!
				}
			});
		} catch (JSONException e) {

		}
	}
    
	public void sendPush(String target, byte[] dataBytes, String objID) {
		try {
			System.out.println("audioDATA: " + dataBytes.length);
			ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
			query.whereEqualTo("username", target);
			ParsePush push = new ParsePush();
			push.setQuery(query);

			long dayInterval = 60 * 60 * 24; // 24 hrs
			String dataStr = "{\"action\": \"com.gilad.oved.holdandtalk.PLAY_MESSAGE\",\"from\":\""
					+ ParseUser.getCurrentUser().getUsername()
					+ "\",\"idid\":\"" + objID
					+ "\",\"alert\": \"Message from "
					+ ParseUser.getCurrentUser().getUsername() + "\"}";
			System.out.println("data STRRRRRRRR : " + dataStr);
			JSONObject data = new JSONObject(dataStr);
			push.setData(data);
			push.setExpirationTimeInterval(dayInterval);
			push.sendInBackground(new SendCallback() {

				@Override
				public void done(ParseException e) {
					System.out.println("play walkie talkie sound - " + e);
				}
			});
		} catch (JSONException e) {

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