package com.gilad.oved.telme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<Group> groups;
    
	boolean firstTouch = true;

    public ExpandableListAdapter(Context context, ArrayList<Group> groups) {
        this.context = context;
        this.groups = groups;
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
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {

        Child child = (Child) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) context
                    .getSystemService(context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.child_item, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.country_name);
        ImageView iv = (ImageView) convertView.findViewById(R.id.flag);

        tv.setText(child.getName().toString());
        iv.setImageResource(child.getImage());

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
		contactImage.setImageResource(R.drawable.bae);

		TextView contactName = (TextView) convertView
				.findViewById(R.id.contactNameLbl);
		contactName.setText(group.getName());

		TextView contactNumber = (TextView) convertView
				.findViewById(R.id.contactNumberLbl);
		contactNumber.setText(group.getNumber());
		
		final String usernameTo = group.getNumber();
        
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
				System.out.println("walkie talkie noise goes here!!! + "
						+ usernameTo);
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

				ParseObject voiceText = new ParseObject("messageData");
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
								+ usernameTo);
						sendPush(usernameTo, audioData);
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
    
	public void sendPush(String target, byte[] dataBytes) {
		try {
			System.out.println("audioDATA: " + dataBytes.length);
			ParseQuery<ParseInstallation> query = ParseInstallation.getQuery();
			query.whereEqualTo("username", target);
			ParsePush push = new ParsePush();
			push.setQuery(query);

			long dayInterval = 60 * 60 * 24; // 24 hrs
			String dataStr = "{\"action\": \"com.gilad.oved.holdandtalk.PLAY_MESSAGE\",\"from\":\""
					+ ParseUser.getCurrentUser().getUsername()
					+ "\",\"alert\": \"Message from "
					+ ParseUser.getCurrentUser().getUsername() + "\"}";
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

}