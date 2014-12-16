package com.gilad.oved.telme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        
        friends = new ArrayList<ParseUser>();
        friendNicknames = new ArrayList<String>();
        friendNumbers = new ArrayList<String>();
        friendPictures = new ArrayList<Bitmap>();
        history = new ArrayList<ArrayList<String>>();
        
		ExpandList = (ExpandableListView) findViewById(R.id.list);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		outputFile = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/myrecording.3gp";
		
        //load list of friends names pics and numbers from local
	    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
	    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/");
	    if (dir.listFiles() != null) {
	    	for (File f : dir.listFiles()) {
    	    	String[] fileinfo = f.getName().split(",");
    	    	friendNicknames.add(fileinfo[0]);
    	    	friendNumbers.add(fileinfo[1]);
    		    File pictureFile = new File(f.getAbsolutePath(), "profilepic.jpg");
    		    Bitmap bmp = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
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
									                    R.drawable.icon);
									  }
			            	     	  friendPictures.add(bmp);
								  } catch (ParseException e1) {
							   		  e1.printStackTrace();
								  }
								  friends.add(user);
					    	      
								  // create new folder for new user...
								  
								    // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
								    File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + user.getString("nickname") + "," + user.getUsername());
								    dir.mkdirs();
								    File pictureFile = new File(dir, "profilepic.jpg");
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
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO); 
			}
		}); 
        
	}
    
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
            Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    
    public static long getSizeInBytes(Bitmap bitmap) {
          return bitmap.getRowBytes() * bitmap.getHeight();
    }
    
    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
               || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        switch(requestCode) { 
        case SELECT_PHOTO:
            if(resultCode == RESULT_OK){  
                Uri selectedImageURI = imageReturnedIntent.getData();
                InputStream imageStream = null;
				try {
					imageStream = getContentResolver().openInputStream(selectedImageURI);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                try {
					selectedImage = decodeUri(selectedImageURI);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
	    	      
                int bytes = (int)getSizeInBytes(selectedImage);
                ByteBuffer buffer = ByteBuffer.allocate(bytes); 
                selectedImage.copyPixelsToBuffer(buffer); 
                byte[] array = buffer.array();
                ParseFile imageDataFile = new ParseFile(array);
                ParseUser.getCurrentUser().put("profilepic", imageDataFile);
                ParseUser.getCurrentUser().saveInBackground();
				Toast.makeText(getApplicationContext(), "Updated the profile", Toast.LENGTH_SHORT).show();
            }
        }
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
		    if (dir.listFiles() != null) {
		    	for (File f : dir.listFiles()) {
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