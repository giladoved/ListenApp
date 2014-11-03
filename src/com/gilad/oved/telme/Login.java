package com.gilad.oved.telme;

import java.util.Locale;

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

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

public class Login extends Activity implements OnClickListener {
	
	Button nextBtn;
	EditText phoneNumberTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
 
		nextBtn = (Button) findViewById(R.id.nextNumberBtn);
		phoneNumberTxt = (EditText) findViewById(R.id.phoneNumberTxt);
		
		if (ParseUser.getCurrentUser() != null) {
			Intent intent = new Intent(Login.this, MainActivity.class);
			Login.this.startActivity(intent);
		}
		
		nextBtn.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
	}
	
	public void sendCodeTo(String number) {
		try {
			ParseQuery pushQuery = ParseInstallation.getQuery();
			ParsePush push = new ParsePush();
			push.setChannel(ParseInstallation.getCurrentInstallation().getInstallationId());
			push.setQuery(pushQuery); // Set our Installation query
			push.setMessage("Your activation code to register for talkit is 47303");
			push.sendInBackground();
			
			Toast.makeText(getApplicationContext(), "Confirmation Text Sent", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(Login.this, Register.class);
		    intent.putExtra("number", number);
			Login.this.startActivity(intent);	
		} catch (Exception e) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			AlertDialog dialog = alertDialogBuilder.create();
			dialog.setMessage(e.getMessage());
			dialog.show();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
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
	
	public void logout() {
		ParseUser user = ParseUser.getCurrentUser();
		if (user != null) {
			ParseUser.logOut();
		}
	}

	@Override
	public void onClick(View v) {
		String number = phoneNumberTxt.getText().toString();
		PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberProto = null;
		try {
			System.out.println("found locale is --- " + Locale.getDefault().getCountry());
			numberProto = phoneUtil.parse(number, Locale.getDefault().getCountry());
		} catch (NumberParseException e) {
		  System.err.println("NumberParseException was thrown: " + e.toString());
		}
		
		if (phoneUtil.isValidNumber(numberProto)) {
			//Parse.push
			sendCodeTo(number);
		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			AlertDialog dialog = alertDialogBuilder.create();
			dialog.setMessage("Please enter a valid phone number");
			dialog.show();
		}
	}

}
