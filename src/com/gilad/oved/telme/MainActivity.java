package com.gilad.oved.telme;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
        friendNicknames = new ArrayList<String>();
        friendNumbers = new ArrayList<String>();
        
		ExpandList = (ExpandableListView) findViewById(R.id.list);
                
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
					    	if (!friendNumbers.contains(userInput.getText().toString().trim())) {
					    	ParseQuery<ParseUser> query = ParseUser.getQuery();
					    	query.whereEqualTo("username", userInput.getText().toString().trim());
					    	query.whereNotEqualTo("username", ParseUser.getCurrentUser().getString("username"));	
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