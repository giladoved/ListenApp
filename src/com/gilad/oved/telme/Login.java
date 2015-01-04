package com.gilad.oved.telme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	private static final String TAG = "ListenApp";
	
	Button nextBtn;
	EditText phoneNumberTxt;
	Handler handler;

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
		}
	}

	//don't allow users to press back button
	@Override
	public void onBackPressed() {
	}
	
	public void sendCodeTo(String number) {
		try {
			Log.d(TAG, "sending message to: " + number);
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

}
