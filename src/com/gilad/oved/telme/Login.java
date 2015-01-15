package com.gilad.oved.telme;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SendCallback;

public class Login extends Activity implements OnClickListener {
	
	private Button nextBtn;
	private EditText phoneNumberTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
 
		nextBtn = (Button) findViewById(R.id.nextNumberBtn);
		nextBtn.setOnClickListener(this);

		phoneNumberTxt = (EditText) findViewById(R.id.phoneNumberTxt);
		
		if (ParseUser.getCurrentUser() != null) {
    		Intent intent = new Intent(Login.this, MainActivity.class);
    		Login.this.startActivity(intent);
    		finish();
		} else {
			createShortcut();
			
			
			File userFile = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/ListenApp/");
			if (userFile != null) {
				String[] children = userFile.list();
				for (int i = 0; i < children.length; i++) {
					new File(userFile, children[i]).delete();
				}
				userFile.delete();
			}
		}
	}

	//don't allow users to press back button
	@Override
	public void onBackPressed() {
	}
	
	private void sendCodeTo(String number) {
		try {
			Log.v(Constants.TAG, "sending message to: " + number);
			ParsePush push = new ParsePush();
			push.setChannel("Gilad" + ParseInstallation.getCurrentInstallation().getInstallationId());
			push.setMessage("47303 - your activation code to register for ListenApp");
			push.sendInBackground(new SendCallback() {
				
				@Override
				public void done(ParseException arg0) {
					Toast.makeText(getApplicationContext(), "Confirmation Text Sent", Toast.LENGTH_LONG).show();
				}
			});
			
			Intent intent = new Intent(Login.this, Register.class);
		    intent.putExtra("number", number);
			startActivity(intent);	
		} catch (Exception e) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			AlertDialog dialog = alertDialogBuilder.create();
			dialog.setMessage(e.getMessage());
			dialog.show();
		}
	}

	@Override
	public void onClick(View v) {
		String number = phoneNumberTxt.getText().toString().trim();
		sendCodeTo(number);
	}
	
	private void createShortcut() {
	    Intent shortcutIntent = new Intent(getApplicationContext(), Login.class);
	    shortcutIntent.setAction(Intent.ACTION_MAIN);

	    Intent addIntent = new Intent();
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "ListenApp");
	    addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
	            Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.icon));
	    addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    getApplicationContext().sendBroadcast(addIntent);
	}

}
