package com.gilad.oved.telme;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class MainApp extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "fXhQsK4oV7zU1VIoJhzHiJzLn28V76x49eZUo16P", "mXhGveWxk6Leo0ZiU1gEu9mR86GFrcEGAMoMZ4GU");
		ParsePush.subscribeInBackground("Gilad" + ParseInstallation.getCurrentInstallation().getInstallationId(), new SaveCallback() {
			  @Override
			  public void done(ParseException e) {
			    if (e == null) {
			      Log.d(Constants.TAG, "successfully subscribed to the broadcast channel.");
			    } else {
			      Log.e(Constants.TAG, "failed to subscribe for push with error: ", e);
			    }
			  }
		});
		ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException arg0) {
				Log.d(Constants.TAG, "saved installation with error: " + arg0);
			}
		});
	}
}
