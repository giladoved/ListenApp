package com.gilad.oved.telme;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryDetail extends ActionBarActivity {

	private String name;
	private String number;    
	private Bitmap picture;  
	
    private ArrayList<String> dates; 
    private ArrayList<String> sentBools;
    private ArrayList<String> paths;
    
    HistoryAdapter historyListAdapter;
    ArrayList<HistoryItem> historyItemList;
    ListView historyListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_history);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		
	    dates = new ArrayList<String>();
	    sentBools = new ArrayList<String>();
	    paths = new ArrayList<String>();
	    		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			name = extras.getString("name");
			number = extras.getString("number");
			//byte[] bytes = getIntent().getByteArrayExtra("picture");
	        //picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
	        dates = extras.getStringArrayList("dates");
			sentBools = extras.getStringArrayList("sentBools");
			paths = extras.getStringArrayList("paths");
		}
		
        getSupportActionBar().setTitle(name);
        
        historyItemList = new ArrayList<HistoryItem>();
	    for(int i = 0; i < dates.size(); i++) {
	            HistoryItem history = new HistoryItem();
	            history.date = dates.get(i);
	            history.sentBool = sentBools.get(i);
	            history.path = paths.get(i);
	            historyItemList.add(history);
	    }
        
        historyListView = (ListView) findViewById(R.id.historyDetailsList);
        historyListAdapter = new HistoryAdapter(HistoryDetail.this, historyItemList);
        historyListView.setAdapter(historyListAdapter);
        historyListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
                    long arg3) {
            	
                HistoryItem history = historyListAdapter.getHistoryItem(pos);
                File file = new File(history.path); // acquire the file from path string
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.optionshistory, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.clearHistory) {
			Toast.makeText(getApplicationContext(), "History Cleared", Toast.LENGTH_SHORT).show();
			File dir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/" + name + "," + number);
		    if (dir.listFiles() != null) {
		    	for (File f : dir.listFiles()) {
		    		if (!f.getName().contains("jpg")) //delete everything except the profile picture
		    			f.delete();
		    	}
		    }
		    
		    dates = new ArrayList<String>();
		    sentBools = new ArrayList<String>();
		    paths = new ArrayList<String>();
		    
		    historyItemList = new ArrayList<HistoryItem>();
		    for(int i = 0; i < dates.size(); i++) {
		            HistoryItem history = new HistoryItem();
		            history.date = dates.get(i);
		            history.sentBool = sentBools.get(i);
		            history.path = paths.get(i);
		            historyItemList.add(history);
		    }
	        
	        historyListAdapter = new HistoryAdapter(HistoryDetail.this, historyItemList);
	        historyListView.setAdapter(historyListAdapter);
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	   public void onBackPressed() {
	      moveTaskToBack(true); 
	      HistoryDetail.this.finish();
	   }
}
