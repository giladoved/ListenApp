package com.gilad.oved.telme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class Register extends Activity {

	Button submitBtn;
	EditText activationCodeTxt;
	EditText nicknameTxt;
	String number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

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
					e.printStackTrace();
				}
			}
		});
	}
	
	public void register(String code) throws Exception {
		code = code.trim();
		if (code.equalsIgnoreCase("47303")) {
			ParseUser user = new ParseUser();
			if (number != null) {
				user.setUsername(number);
				user.setPassword(number);
				user.put("nickname", nicknameTxt.getText().toString().trim());
				Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_SHORT).show();
				user.signUpInBackground(new SignUpCallback() {
					@Override
					public void done(ParseException e) {
						if (e == null) {
							ParseInstallation installation = ParseInstallation
									.getCurrentInstallation();
							installation.put("user",ParseUser.getCurrentUser());
							installation.put("username",number);
							installation.saveInBackground();
							
							Toast.makeText(getApplicationContext(), "All set and ready to go!", Toast.LENGTH_LONG).show();
							Intent intent = new Intent(Register.this, MainActivity.class);
							Register.this.startActivity(intent);
						} else {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());
							AlertDialog dialog = alertDialogBuilder.create();
							dialog.setMessage(e.getMessage());
							dialog.show();
						}
					}
				});
			}
		}
	}
	
	@Override
	public void onBackPressed() {
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.register, menu);
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
		return super.onOptionsItemSelected(item);
	}
}
