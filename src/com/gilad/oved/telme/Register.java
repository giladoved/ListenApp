package com.gilad.oved.telme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class Register extends Activity {

	private Button submitBtn;
	private EditText activationCodeTxt;
	private EditText nicknameTxt;
	private String number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		//get the phone number that the user entered from the last screen
		Intent intent = getIntent();
		number = intent.getStringExtra("number");

		submitBtn = (Button) findViewById(R.id.submitNumberBtn);
		activationCodeTxt = (EditText) findViewById(R.id.activationCodeTxt);
		nicknameTxt = (EditText) findViewById(R.id.nicknameTxt);

		submitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					register(activationCodeTxt.getText().toString().trim());
				} catch (Exception e) {
					Log.e(Constants.TAG, e.getLocalizedMessage());
				}
			}
		});
	}
	
	private void register(String code) throws Exception {
		code = code.trim();
		//hardcoded activation code... improve later
		if (code.equalsIgnoreCase("47303")) {
			ParseUser user = new ParseUser(); 
			if (number != null) {
				user.setUsername(number);
				user.setPassword(number);
				user.put("nickname", nicknameTxt.getText().toString().trim());
				//change to loading wheel soon
				Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
				user.signUpInBackground(new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							ParseInstallation installation = ParseInstallation
									.getCurrentInstallation();
							installation.put("user",ParseUser.getCurrentUser());
							installation.put("username",number);
							installation.saveInBackground(new SaveCallback() {
								
								@Override
								public void done(ParseException arg0) {
									if (arg0 == null) {
										Toast.makeText(getApplicationContext(), "All set and ready to go!", Toast.LENGTH_LONG).show();
									}
								}
							});
							
							Intent intent = new Intent(Register.this, MainActivity.class);
							Register.this.startActivity(intent);
						} else {
							Log.e(Constants.TAG, e.getLocalizedMessage());
						}
					}
				});
			}
		}
	}
	
	//don't let the users go back
	@Override
	public void onBackPressed() {
		
	}
}
