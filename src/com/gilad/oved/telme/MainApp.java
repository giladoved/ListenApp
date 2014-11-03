package com.gilad.oved.telme;

import android.app.Application;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainApp extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "fXhQsK4oV7zU1VIoJhzHiJzLn28V76x49eZUo16P", "mXhGveWxk6Leo0ZiU1gEu9mR86GFrcEGAMoMZ4GU");
		ParsePush.subscribeInBackground(ParseInstallation.getCurrentInstallation().getInstallationId(), new SaveCallback() {
			  @Override
			  public void done(ParseException e) {
			    if (e != null) {
			      Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
			    } else {
			      Log.e("com.parse.push", "failed to subscribe for push", e);
			    }
			  }
		});
	}
}
