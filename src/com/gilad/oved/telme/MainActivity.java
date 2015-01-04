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
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends ActionBarActivity {
	
	private ExpandableListAdapter listAdapter;
	private ArrayList<Group> listItems;
	private ExpandableListView expandableList;
    
	private ArrayList<String> friendNicknames;
	private ArrayList<String> friendNumbers;    
	private ArrayList<Bitmap> friendPictures;   
        
	private static final int SELECT_PHOTO = 999;
    private static MediaRecorder mediaRecorder;
    public static String recordingOutputFile = null;
	
    private ProgressDialog progress;
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        friendNicknames = new ArrayList<String>();
        friendNumbers = new ArrayList<String>();
        friendPictures = new ArrayList<Bitmap>();
        expandableList = (ExpandableListView) findViewById(R.id.list);
                
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		recordingOutputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/recording.3gp";
		
        //load list of friends names pics and numbers from local
	    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/Pictures/");
	    if (dir.listFiles() != null) {
	    	for (File f : dir.listFiles()) {
    	    	String[] fileinfo = f.getName().split(",");
    	    	friendNicknames.add(fileinfo[0]);
    	    	String num = fileinfo[1].substring(0, fileinfo[1].length()-4); //remove the .jpg part
    	    	friendNumbers.add(num);
    		    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
    		    friendPictures.add(bmp);
	    	}
	    }
    	    
	    listItems = setGroupsData();
        listAdapter = new ExpandableListAdapter(MainActivity.this, listItems, friendNicknames, friendNumbers, friendPictures, expandableList);
        expandableList.setAdapter(listAdapter);
	}
    
    private void addNewContact() {
		final Context context = MainActivity.this;
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View promptsView = layoutInflater.inflate(R.layout.add_contact, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.addNumberTxt);
		userInput.setInputType(InputType.TYPE_CLASS_PHONE);

		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("OK",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
			        progress = ProgressDialog.show(context, "Loading...", "");
			    	final String lookingForNumber = userInput.getText().toString().trim();
			    	if (!friendNumbers.contains(lookingForNumber)) {
			    	ParseQuery<ParseUser> query = ParseUser.getQuery();
			    	query.whereEqualTo("username", lookingForNumber);
			    	query.getFirstInBackground(new GetCallback<ParseUser>() {
			    	  public void done(final ParseUser user, ParseException e) {
			    	      //ask if the user wants to invite the contact if they aren't registered
			    		  progress.dismiss();
			    		  if (user == null) {				    	        	
		    	        	//http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-in-android
		    	        	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    	        	    @Override
		    	        	    public void onClick(DialogInterface dialog, int which) {
		    	        	    	if (which == DialogInterface.BUTTON_POSITIVE) {
		    	        	        	HashMap<String, Object> params = new HashMap<String, Object>();
		    	        	        	params.put("fromNumber", ParseUser.getCurrentUser().getUsername());
		    	        	        	params.put("fromName", ParseUser.getCurrentUser().getString("nickname"));
		    	        	        	params.put("toNumber", lookingForNumber);
		    	        	        	Log.v(Constants.TAG, "invitation params are : " + params);
		    	        	        	//call cloud function that sends text using twilio
		    	        	        	ParseCloud.callFunctionInBackground("inviteWithTwilio", params, new FunctionCallback<String>() {
		    	        	        		  public void done(String result, ParseException e) {
		    	        	        		    if (e == null) {
		    	        	        		    	Toast.makeText(getApplicationContext(), "Your SMS invitation has been sent!", Toast.LENGTH_SHORT).show();
		    	        	        		    } else {
		    	        	        		    	Log.e(Constants.TAG, e.getLocalizedMessage());
		    	        	        		    }
		    	        	        		  }
		    	        	        		});
		    	        	    	}
		    	        	    }
		    	        	};

		    	        	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		    	        	builder.setMessage(userInput.getText().toString().trim() + " is not a registered number. Would you like to invite them to use ListenApp?").setPositiveButton("Yes", dialogClickListener)
		    	        	    .setNegativeButton("No", dialogClickListener).show();
			    	    
			    	    } else {
			    	    	//add the number to their local friends list
			    	      friendNumbers.add(user.getUsername());
			    	      friendNicknames.add(user.getString("nickname"));
            			  ParseFile foundPic = user.getParseFile("profilepic");
            			  Bitmap bmp = null;
						  try {
							  if (foundPic != null) {
								  bmp = BitmapFactory.decodeByteArray(foundPic.getData(), 0, foundPic.getData().length);
							  } else {
									bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.userprofile);
							  }
	            	     	  friendPictures.add(bmp);
						  } catch (ParseException e1) {
					   		  Log.e(Constants.TAG, e1.getLocalizedMessage());
						  }
			    	      
						  // create new folder for new user...
						    File dirUser = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + user.getString("nickname") + "," + user.getUsername());
						    dirUser.mkdirs();
						  
						    //add their picture to the folder of pictures
						    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/Pictures/");
						    dir.mkdirs();
						    File pictureFile = new File(dir, user.getString("nickname") + "," + user.getUsername() + ".jpg");
						    if (pictureFile.exists()) pictureFile.delete(); //allow overriding if they update their picture
						    try {
						    	FileOutputStream out = new FileOutputStream(pictureFile);
						    	bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
						    	out.flush();
						    	out.close();
						    } catch (Exception e1) {
						   		  Log.e(Constants.TAG, e1.getLocalizedMessage());
						    }
						    
						    listItems = setGroupsData();
						    listAdapter = new ExpandableListAdapter(MainActivity.this, listItems, friendNicknames, friendNumbers, friendPictures, expandableList);
						    expandableList.setAdapter(listAdapter);
						    listAdapter.notifyDataSetChanged();
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case SELECT_PHOTO:
            if(resultCode == RESULT_OK){  
		        progress = ProgressDialog.show(this, "Loading...", "");
            	Bitmap selectedImage = null;
            	try {
					Uri imageUri = imageReturnedIntent.getData();
					InputStream imageStream = getContentResolver().openInputStream(imageUri);
					selectedImage = BitmapFactory.decodeStream(imageStream);
				} catch (FileNotFoundException e) {
					Log.e(Constants.TAG, e.getLocalizedMessage());
				}
	    	      
            	ByteArrayOutputStream stream = new ByteArrayOutputStream();
				selectedImage.compress(Bitmap.CompressFormat.PNG, 25, stream); //compresses the image by 75%
				byte[] image = stream.toByteArray();
				ParseFile file = new ParseFile(image);
				file.saveInBackground();
				
                ParseUser.getCurrentUser().put("profilepic", file);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
					
					@Override
					public void done(ParseException e) {
						progress.dismiss();
						if (e == null)
							Toast.makeText(getApplicationContext(), "Updated the profile", Toast.LENGTH_SHORT).show();
						else
							Log.e(Constants.TAG, e.getLocalizedMessage());
					}
				});
            }
        }
    }
    
    //options menu and action bar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.optionsmenu, menu);
        inflater.inflate(R.menu.actionbar, menu);
        return true;
    }
    
    //detect options menu click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.editProfilePicture:
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    		return true;
    	case R.id.addNewContact:
    		addNewContact();
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    //don't let the users go back
    @Override
	public void onBackPressed() {
	}
    
    //start recording
    public static void startRecording() {
    	mediaRecorder = new MediaRecorder();
    	mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    	mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    	mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    	mediaRecorder.setOutputFile(recordingOutputFile);
    	mediaRecorder.setMaxDuration(1000 * 60 * 5);
    	mediaRecorder.setOnInfoListener(new OnInfoListener() {
			
			@Override
			public void onInfo(MediaRecorder mr, int what, int extra) {
				switch (what) {
				case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
					Log.d(Constants.TAG, "MEDIA_RECORDER_INFO_UNKNOWN");
					break;
				case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
					Log.d(Constants.TAG, "MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
					break;
				case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
					Log.d(Constants.TAG, "MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED");
					break;
				}
			}
		});
    	
    	mediaRecorder.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public void onError(MediaRecorder mr, int what, int extra) {
				switch (what) {
				case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
					Log.e(Constants.TAG, "MEDIA_ERROR_SERVER_DIED");
					break;
				case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
					Log.e(Constants.TAG, "MEDIA_RECORDER_ERROR_UNKNOWN");
					break;
				}
			}
		});
    	
		try {
			mediaRecorder.prepare();
			mediaRecorder.start();
		} catch (IllegalStateException e) {
			Log.e(Constants.TAG, e.getLocalizedMessage());
		} catch (IOException e) {
			Log.e(Constants.TAG, e.getLocalizedMessage());
		}
	}

    //stop the recording
	public static void stopRecording() {
		try {
			mediaRecorder.stop();
			mediaRecorder.reset();
			mediaRecorder.release();
			mediaRecorder = null;
		} catch (IllegalStateException e) {
			Log.e(Constants.TAG, e.getLocalizedMessage());
		} catch(RuntimeException e){
			Log.e(Constants.TAG, e.getLocalizedMessage());
		}
	}
    
	private ArrayList<Group> setGroupsData() {        
        ArrayList<Child> ch_list = new ArrayList<Child>();
        ArrayList<Group> list = new ArrayList<Group>();
        int counter = 0;
        
		for (String group_name : friendNicknames) {
			Group gru = new Group();
			gru.setName(group_name);
			gru.setNumber(friendNumbers.get(counter));
			gru.setPicture(friendPictures.get(counter));
			
	        ArrayList<String> dates = new ArrayList<String>();
	        ArrayList<String> sentBools = new ArrayList<String>();
	        ArrayList<String> paths = new ArrayList<String>();
			
			ch_list = new ArrayList<Child>();
			Child ch = new Child();
			
		    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + friendNicknames.get(counter) + "," + friendNumbers.get(counter));
		    File[] files = dir.listFiles();
		    Arrays.sort(files, new Comparator<File>(){
		        public int compare(File f1, File f2) {
		            return -Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()); //newest first
		        } 
		    });
		    		    
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
							Log.e(Constants.TAG, e.getLocalizedMessage());
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