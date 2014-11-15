package com.gilad.oved.telme;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ParseFacebookUtils.Permissions.Friends;

public class MainActivity extends Activity {
	
	ExpandableListAdapter ExpAdapter;
    ArrayList<Group> ExpListItems;
    ExpandableListView ExpandList;
    
    Button addBtn;
    
    ArrayList<String> friendNicknames;
    ArrayList<String> friendNumbers;
    
	public static final String PREFS_NAME = "MyPrefsFile";
    private static MediaRecorder myAudioRecorder;
	public static String outputFile = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
        friendNicknames = new ArrayList<String>();
        friendNumbers = new ArrayList<String>();
        
		ExpandList = (ExpandableListView) findViewById(R.id.list);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		outputFile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/myrecording.3gp";
                
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
          public void done(ParseUser user, ParseException e) {
            if (e == null) {
            	if (user != null) {
            		List<Object> foundFriendsNums = user.getList("friendsNumbers");
            		System.out.println("found numbers is actually skrillex: : :: : " + foundFriendsNums);
            		if (foundFriendsNums != null) {
            			for (Object obj : foundFriendsNums) {
            				friendNumbers.add(obj != null ? obj.toString() : null);
            			}
            			System.out.println("friendNumber is : " + friendNumbers.toString());
            			List<Object> foundFriendsNames = user.getList("friendsNames");
                		if (foundFriendsNames != null) {
                			for (Object obj : foundFriendsNames) {
                				friendNicknames.add(obj != null ? obj.toString() : null);
                			}
                			System.out.println("friendNames is : " + friendNicknames.toString());
                		}
            		} else {
            			System.out.println("not going through with skrillex::::: " + friendNicknames);
            		}
            	}
            } else {
            	System.out.println("Error: " + e);
            }
            ExpListItems = SetStandardGroups();
            ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems);
            ExpandList.setAdapter(ExpAdapter);
          }
        });
        
        addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Context context = MainActivity.this;
				LayoutInflater li = LayoutInflater.from(context);
				View promptsView = li.inflate(R.layout.add_contact, null);
 
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 				alertDialogBuilder.setView(promptsView);
 
				final EditText userInput = (EditText) promptsView
						.findViewById(R.id.addNumberTxt);

				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
					    	String input = userInput.getText().toString().trim();
					    	System.out.println("intput is : " + input);
					    	if (!friendNumbers.contains(input)) {
					    	ParseQuery<ParseUser> query = ParseUser.getQuery();
					    	query.whereEqualTo("username", input);
					    	query.getFirstInBackground(new GetCallback<ParseUser>() {
					    	  public void done(ParseUser user, ParseException e) {
					    	    if (user == null) {
				    	        	Toast.makeText(getApplicationContext(), "Could not find the phone number: " + userInput.getText().toString().trim(), Toast.LENGTH_SHORT).show();
					    	    } else {
					    	      Log.d("telme", "Retrieved the object.");

					    	      friendNumbers.add(user.getUsername());
					    	      friendNicknames.add(user.getString("nickname"));
					    	      
		    	            		//update currrent user friend list on parse
		    	            		ParseQuery<ParseUser> query = ParseUser.getQuery();
		    	            		query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
		    	            			public void done(ParseUser foundUser, ParseException e) {
		    	            				if (e == null) {
		    	            					foundUser.put("friendsNames", friendNicknames);
		    	            					foundUser.put("friendsNumbers", friendNumbers);
		    	            					foundUser.saveInBackground();
		    	            					System.out.println("nigga we made it..." + friendNumbers.toString());
		    	            					System.out.println("bithc pelease: " + friendNicknames.toString());
		    	            			        ExpListItems = SetStandardGroups();
		    	            			        ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems);
		    	            			        ExpandList.setAdapter(ExpAdapter);
		    	            			        ExpAdapter.notifyDataSetChanged();
		    	            				}
		    	            			}
		    	            		});
					    	    }
					    	  }
					    	});
					    }
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					    }
					  });

				alertDialogBuilder.create().show();
			}
		});
	}
    
    @Override
	public void onBackPressed() {
	}
    
    public static void start() {
		myAudioRecorder = new MediaRecorder();
		myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
		myAudioRecorder.setOutputFile(outputFile);
		try {
			myAudioRecorder.prepare();
			myAudioRecorder.start();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void stop() {
		try {
			myAudioRecorder.stop();
		} catch (IllegalStateException e) {
			System.out.println("E is: ----- " + e.getLocalizedMessage());
		}
		myAudioRecorder.reset();
		myAudioRecorder.release();
		myAudioRecorder = null;
	}
    
	public ArrayList<Group> SetStandardGroups() {
        String country_names[] = { "Brazil", "Mexico", "Croatia", "Cameroon",
                "Netherlands", "chile", "Spain", "Australia", "Colombia",
                "Greece", "Ivory Coast", "Japan", "Costa Rica", "Uruguay",
                "Italy", "England", "France", "Switzerland", "Ecuador",
                "Honduras", "Agrentina", "Nigeria", "Bosnia and Herzegovina",
                "Iran", "Germany", "United States", "Portugal", "Ghana",
                "Belgium", "Algeria", "Russia", "Korea Republic" };

        int Images[] = { R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher, R.drawable.ic_launcher, R.drawable.ic_launcher,
                R.drawable.ic_launcher };

        ArrayList<Group> list = new ArrayList<Group>();

        ArrayList<Child> ch_list;

        int size = 4;
        int j = 0;
        int counter = 0;
        
        for (String group_name : friendNicknames) {
            Group gru = new Group();
            gru.setName(group_name);
            gru.setNumber(friendNumbers.get(counter));
            
            ch_list = new ArrayList<Child>();
            for (; j < size; j++) {
                Child ch = new Child();
                ch.setName(country_names[j]);
                ch.setImage(Images[j]);
                ch_list.add(ch);
            }
            gru.setItems(ch_list);
            list.add(gru);

            size = size + 4;
            counter++;
        }

        return list;
    }
}