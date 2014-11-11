package com.gilad.oved.telme;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
        ExpListItems = SetStandardGroups();
        ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
                
        ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
        query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
          public void done(ParseObject object, ParseException e) {
            if (e == null) {
            	List<Object> foundFriendsNums = object.getList("friendsNumbers");
            	for (Object obj : foundFriendsNums) {
            	    friendNumbers.add(obj != null ? obj.toString() : null);
            	}
            	
            	List<Object> foundFriendsNames = object.getList("friendsNames");
            	for (Object obj : foundFriendsNames) {
            	    friendNicknames.add(obj != null ? obj.toString() : null);
            	}
            } else {
            	//Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_LONG).show();
            }
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
					    	ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
					    	query.whereEqualTo("username", userInput.getText().toString().trim());
					    	query.whereNotEqualTo("username", ParseUser.getCurrentUser().getString("username"));
					    	query.findInBackground(new FindCallback<ParseObject>() {
					    	    public void done(List<ParseObject> foundUsers, ParseException e) {
					    	        if (e == null) {
					    	            if (foundUsers.size() > 0) {
						    	            System.out.println("!!! Retrieved " + foundUsers);
					    	            	ParseObject foundUser = foundUsers.get(0);
					    	            	friendNicknames.add(foundUser.getString("friendsNames"));
					    	            	friendNumbers.add(foundUser.getString("friendsNumbers"));
					    	            	
					    	            	//update currrent user friend list on parse
					    	            	ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
					    	            	query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseObject>() {
					    	            	  public void done(ParseObject foundUser, ParseException e) {
					    	            	    if (e == null) {
					    	            	      foundUser.put("friendsNames", friendNicknames);
					    	            	      foundUser.put("friendsNumbers", friendNumbers);
					    	            	      foundUser.saveInBackground();
					    	            	    }
					    	            	  }
					    	            	});
					    	            } else {
						    	        	Toast.makeText(getApplicationContext(), "Could not find the phone number: " + userInput.getText().toString().trim(), Toast.LENGTH_SHORT).show();
						    	            System.out.println("!!! Error: " + e.getMessage());
						    	        }
					    	        } 
					    	    }
					    	});
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
        
        //get array of usernames and nicknames form storage
        //addBtn.setonclick(add username and nickname to storage (parse property friends[]) then reload page)
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
            gru.setNumber(friendNicknames.get(counter));
            
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