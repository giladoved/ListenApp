package com.gilad.oved.telme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
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
    ArrayList<ParseFile> friendPictures;
    
    ArrayList<ArrayList<String>> history;

    
    private static final int SELECT_PHOTO = 100;
	public static final String PREFS_NAME = "MyPrefsFile";
    private static MediaRecorder myAudioRecorder;
	public static String outputFile = null;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
        friendNicknames = new ArrayList<String>();
        friendNumbers = new ArrayList<String>();
        friendPictures = new ArrayList<ParseFile>();
        history = new ArrayList<ArrayList<String>>();
        
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
                			/*List<Object> foundFriendsPictures = user.getList("friendsPictures");
                			if (foundFriendsPictures != null) {
                				for (Object obj : foundFriendsPictures) {
                					ParseFile parseFile = (ParseFile) obj;
  					    	      	byte[] data;
  					    	      	Bitmap bmp;
  					    	      	try {
  					    	      		data = parseFile.getData();
  					    	      		bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
  	                					friendPictures.add(parseFile);
  					    	      	} catch (ParseException e1) {
  					    	      		e1.printStackTrace();
  					    	      	}
                				}
                			}*/
                		}
            		} else {
            			System.out.println("not going through with skrillex::::: " + friendNicknames);
            		}
            	}
            } else {
            	System.out.println("Error: " + e);
            }
    	    
            ExpListItems = SetStandardGroups();
            ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems, friendNicknames, friendNumbers, friendPictures, ExpandList);
            ExpandList.setAdapter(ExpAdapter);
          }
        });
        
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
				    	        	Toast.makeText(getApplicationContext(), "Could not find the phone number: " + userInput.getText().toString().trim(), Toast.LENGTH_SHORT).show();
					    	    } else {
					    	      Log.d("telme", "Retrieved the object.");

					    	      friendNumbers.add(user.getUsername());
					    	      friendNicknames.add(user.getString("nickname"));
					    	      /*ParseFile parseFile = user.getParseFile("profilepic");
					    	      byte[] data;
					    	      Bitmap bmp;
					    	   	  try {
							   	      if (parseFile != null) {
							   	    	  data = parseFile.getData();
						        		  bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
						   	    		  friendPictures.add(parseFile);
							   	      } else {
							   	    	  bmp = BitmapFactory.decodeResource(context.getResources(),
		                                           R.drawable.bae);
							   	    	  int bytes = (int)getSizeInBytes(bmp);
							   	    	  ByteBuffer buffer = ByteBuffer.allocate(bytes); 
							   	    	  bmp.copyPixelsToBuffer(buffer); 
							   	    	  byte[] array = buffer.array();
							   	    	  parseFile = new ParseFile(array);
							   	    	  data = parseFile.getData();
						        		  bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
						   	    		  friendPictures.add(parseFile);
							   	      }
					   	    	  } catch (ParseException e1) {
					   	    		  e.printStackTrace();
				    	    	  }*/
					    	   	  
		    	            		//update currrent user friend list on parse
		    	            		ParseQuery<ParseUser> query = ParseUser.getQuery();
		    	            		query.getInBackground(ParseUser.getCurrentUser().getObjectId(), new GetCallback<ParseUser>() {
		    	            			public void done(ParseUser foundUser, ParseException e) {
		    	            				if (e == null) {
		    	            					foundUser.put("friendsNames", friendNicknames);
		    	            					foundUser.put("friendsNumbers", friendNumbers);
		    	            					//System.out.println("friend pictures ssss: " + friendPictures);
		    	            					//foundUser.put("friendsPictures", friendPictures);
		    	            					foundUser.saveInBackground();
		    	            					System.out.println("nigga we made it..." + friendNumbers.toString());
		    	            					System.out.println("bithc pelease: " + friendNicknames.toString());
		    	            					//System.out.println("yeahhhh pelease: " + friendPictures.toString());
		    	            			        ExpListItems = SetStandardGroups();
		    	            			        ExpAdapter = new ExpandableListAdapter(MainActivity.this, ExpListItems, friendNicknames, friendNumbers, friendPictures, ExpandList);
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
				Toast.makeText(getApplicationContext(), "Edit the profile", Toast.LENGTH_SHORT).show();
				
				FileOutputStream out = null;
				try {
					String path = Environment.getExternalStorageDirectory().toString();
					File filename = new File(path, "profilepic.jpg"); // the File to save to
				    out = new FileOutputStream(filename);
				    selectedImage.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
				    // PNG is a lossless format, the compression factor (100) is ignored
				} catch (Exception e) {
				    e.printStackTrace();
				} finally {
				    try {
				        if (out != null) {
				            out.close();
				        }
				    } catch (IOException e) {
				        e.printStackTrace();
				    }
				}
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
        String country_names[] = { "Brazil", "Mexico", "Croatia", "Cameroon",
                "Netherlands", "chile", "Spain", "Australia", "Colombia",
                "Greece", "Ivory Coast", "Japan", "Costa Rica", "Uruguay",
                "Italy", "England", "France", "Switzerland", "Ecuador",
                "Honduras", "Agrentina", "Nigeria", "Bosnia and Herzegovina",
                "Iran", "Germany", "United States", "Portugal", "Ghana",
                "Belgium", "Algeria", "Russia", "Korea Republic" };
        final ArrayList<String> childListDemo = new ArrayList<String>();
        for (int i = 0; i < country_names.length; ++i) {
          childListDemo.add(country_names[i]);
        }

        ArrayList<Child> ch_list;
        ArrayList<Group> list = new ArrayList<Group>();
        int counter = 0;
        
        for (String group_name : friendNicknames) {
            Group gru = new Group();
            gru.setName(group_name);
            gru.setNumber(friendNumbers.get(counter));
            
            ParseFile parseFile = friendPictures.get(counter);
  	      	byte[] data;
  	      	Bitmap bmp;
  	      	try {
  	      		data = parseFile.getData();
  	      		bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
					friendPictures.add(parseFile);
		        gru.setPicture(bmp);
  	      	} catch (ParseException e1) {
  	      		e1.printStackTrace();
  	      	}
            
            ch_list = new ArrayList<Child>();
            Child ch = new Child();
            ch.setList(childListDemo);
            ch_list.add(ch);
            gru.setItems(ch_list);

            list.add(gru);
            counter++;
        }

        return list;
    }
}