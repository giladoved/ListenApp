package com.gilad.oved.telme;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MainActivity extends Activity {
	
	ExpandableListAdapter ExpAdapter;
    ArrayList<Group> ExpListItems;
    ExpandableListView ExpandList;
    
    Button addBtn;
    Button editProfileBtn;
    public static int profileCheckerCounter;
    
    ArrayList<String> friendNicknames;
    ArrayList<String> friendNumbers;    
    ArrayList<Bitmap> friendPictures;   
    ArrayList<ParseUser> friends;
    
    ArrayList<ArrayList<String>> history;
    
    private static final int SELECT_PHOTO = 100;
	public static final String PREFS_NAME = "MyPrefsFile";
    private static MediaRecorder myAudioRecorder;
	public static String outputFile = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		/*profileCheckerCounter++;
		if (profileCheckerCounter >= 100) {
			profileCheckerCounter = 0;
			ParseQuery<ParseUser> query = ParseUser.getQuery();
	    	query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
	    	query.findInBackground(new FindCallback<ParseUser>() {
	    	    public void done(List<ParseUser> userList, ParseException e) {
	    	    	for (ParseUser user : userList) {
	    	    		ParseFile foundPic = user.getParseFile("profilepic");
	      			  	Bitmap bmp = null;
	      			  	try {
						  if (foundPic != null) {
							  bmp = BitmapFactory.decodeByteArray(foundPic.getData(), 0, foundPic.getData().length);
						  }
	      			  	} catch (ParseException e1) {
	      			  		e1.printStackTrace();
	      			  	}
					    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
					    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/Pictures/");
					    dir.mkdirs();
					    File pictureFile = new File(dir, user.getString("nickname") + "," + user.getUsername() + ".jpg");
					    if (pictureFile.exists()) pictureFile.delete(); 
					    try {
					    	FileOutputStream out = new FileOutputStream(pictureFile);
					    	bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
					    	out.flush();
					    	out.close();
					    } catch (Exception e1) {
					    	e1.printStackTrace();
					    }
	    	    	}
	    	    }
	    	});*/
		
        friends = new ArrayList<ParseUser>();
        friendNicknames = new ArrayList<String>();
        friendNumbers = new ArrayList<String>();
        friendPictures = new ArrayList<Bitmap>();
        history = new ArrayList<ArrayList<String>>();
        
		ExpandList = (ExpandableListView) findViewById(R.id.list);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		outputFile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/ListenApp/myrecording.3gp";
		
        //load list of friends names pics and numbers from local
	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
	    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/Pictures/");
	    if (dir.listFiles() != null) {
	    	for (File f : dir.listFiles()) {
    	    	String[] fileinfo = f.getName().split(",");
    	    	System.out.println("fileinfo: " + Arrays.toString(fileinfo));
    	    	friendNicknames.add(fileinfo[0]);
    	    	String num = fileinfo[1].substring(0, fileinfo[1].length()-4);
    	    	System.out.println("num is : " + num);
    	    	friendNumbers.add(num);
    		    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
    		    friendPictures.add(bmp);
	    	}
	    }
    	    
        ExpListItems = SetStandardGroups();
        ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems, friendNicknames, friendNumbers, ExpandList);
        ExpandList.setAdapter(ExpAdapter);
            
        addBtn = (Button) findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Context context = MainActivity.this;
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
				    	        	Toast.makeText(getApplicationContext(), userInput.getText().toString().trim() + " is not a registered number", Toast.LENGTH_SHORT).show();
					    	    } else {
					    	      Log.d("telme", "Retrieved the object.");

					    	      friendNumbers.add(user.getUsername());
					    	      friendNicknames.add(user.getString("nickname"));
		            			  ParseFile foundPic = user.getParseFile("profilepic");
		            			  Bitmap bmp = null;
								  try {
									  if (foundPic != null) {
										  System.out.println("found pic is " + foundPic);
										  bmp = BitmapFactory.decodeByteArray(foundPic.getData(), 0, foundPic.getData().length);
									  } else {
										  System.out.println("found pic is the icon biic");
											bmp = BitmapFactory.decodeResource(context.getResources(),
									                    R.drawable.userprofile);
									  }
			            	     	  friendPictures.add(bmp);
								  } catch (ParseException e1) {
							   		  e1.printStackTrace();
								  }
								  friends.add(user);
					    	      
								  // create new folder for new user...
								    File dirUser = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + user.getString("nickname") + "," + user.getUsername());
								    dirUser.mkdirs();
								  
								    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
								    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/Pictures/");
								    dir.mkdirs();
								    File pictureFile = new File(dir, user.getString("nickname") + "," + user.getUsername() + ".jpg");
								    if (pictureFile.exists()) pictureFile.delete(); 
								    try {
								    	FileOutputStream out = new FileOutputStream(pictureFile);
								    	bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
								    	out.flush();
								    	out.close();
								    } catch (Exception e1) {
								    	e1.printStackTrace();
								    }
								    
								    
								  ExpListItems = SetStandardGroups();
	            			        ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems, friendNicknames, friendNumbers, ExpandList);
	            			        ExpandList.setAdapter(ExpAdapter);
	            			        ExpAdapter.notifyDataSetChanged();
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
        
        editProfileBtn = (Button) findViewById(R.id.editProfile);
        editProfileBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				System.out.println("button clicked :)");
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);
			}
		}); 
		
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case SELECT_PHOTO:
            if(resultCode == RESULT_OK){  
            	System.out.println("choossing phooto: " + resultCode);
            	Bitmap selectedImage = null;
            	try {
					Uri imageUri = imageReturnedIntent.getData();
					InputStream imageStream = getContentResolver().openInputStream(imageUri);
					selectedImage = BitmapFactory.decodeStream(imageStream);
				} catch (FileNotFoundException e) {
					e.printStackTrace(); 
				}
	    	      
            	ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// Compress image to lower quality scale 1 - 100
				selectedImage.compress(Bitmap.CompressFormat.PNG, 25, stream);
				byte[] image = stream.toByteArray();
				ParseFile file = new ParseFile(image);
				file.saveInBackground();
				
                ParseUser.getCurrentUser().put("profilepic", file);
                ParseUser.getCurrentUser().saveInBackground();
				Toast.makeText(getApplicationContext(), "Updated the profile", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
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
        ArrayList<String> dates = new ArrayList<String>();
        ArrayList<String> sentBools = new ArrayList<String>();
        ArrayList<String> paths = new ArrayList<String>();
        
        ArrayList<Child> ch_list = new ArrayList<Child>();
        ArrayList<Group> list = new ArrayList<Group>();
        int counter = 0;
        
		for (String group_name : friendNicknames) {
			Group gru = new Group();
			gru.setName(group_name);
			gru.setNumber(friendNumbers.get(counter));
			gru.setPicture(friendPictures.get(counter));
			
			ch_list = new ArrayList<Child>();
			Child ch = new Child();
			
		    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + friendNicknames.get(counter) + "," + friendNumbers.get(counter));
		    File[] files = dir.listFiles();
		    Arrays.sort(files, new Comparator<File>(){
		        public int compare(File f1, File f2)
		        {
		            return -Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		        } });
		    
		    if (files != null) {
		    	for (File f : files) {
					if (!f.getName().contains("jpg")) {
						String[] fileinfo = f.getName().split(",");
						String dateStr = fileinfo[0];
						
						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
						Date convertedDate = new Date();
						try {
							convertedDate = dateFormat.parse(dateStr);
						} catch (java.text.ParseException e) {
							e.printStackTrace();
						}
						
					    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss MMM d");
					    String formattedDateString = formatter.format(convertedDate); 
						dates.add(formattedDateString);
						String sentBool = fileinfo[1];
						sentBools.add(sentBool);
						paths.add(f.getAbsolutePath());
					}
		    	}
		    }
		    
			ch.setDates(dates);
			ch.setSentBools(sentBools);
			ch.setPaths(paths);
			ch_list.add(ch);
			gru.setItems(ch_list);

			list.add(gru);
			counter++;
		}

		return list;
	}
}