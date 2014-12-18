package com.gilad.oved.telme;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class Login extends Activity implements OnClickListener {
	
	Button nextBtn;
	EditText phoneNumberTxt;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
 
		nextBtn = (Button) findViewById(R.id.nextNumberBtn);
		phoneNumberTxt = (EditText) findViewById(R.id.phoneNumberTxt);
		
		handler = new Handler(new Handler.Callback() {
		    @Override
		    public boolean handleMessage(Message msg) {
		        switch (msg.what) {
		            case 1:
		        		Intent intent = new Intent(Login.this, MainActivity.class);
		        		Login.this.startActivity(intent);
		            default:
		                break;
		        }
		        return false;
		    }
		});
		
		nextBtn.setOnClickListener(this);
	}

	public void onPostResume() {
		super.onPostResume();
		if (ParseUser.getCurrentUser() != null) {
			handler.sendEmptyMessageDelayed(1, 1000);
		}
	}

	@Override
	public void onBackPressed() {
	}
	
	public void sendCodeTo(String number) {
		try {
			System.out.println("sending message to: " + number);
			ParsePush push = new ParsePush();
			push.setChannel("Gilad" + ParseInstallation.getCurrentInstallation().getInstallationId());
			push.setMessage("47303 - your activation code to register for ListenApp");
			push.sendInBackground();
			
			Toast.makeText(getApplicationContext(), "Confirmation Text Sent", Toast.LENGTH_LONG).show();
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
		String number = phoneNumberTxt.getText().toString().trim();
		/*PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
		PhoneNumber numberProto = null;
		try {
			System.out.println("found locale is --- " + Locale.getDefault().getCountry());
			numberProto = phoneUtil.parse(number, Locale.getDefault().getCountry());
		} catch (NumberParseException e) {
		  System.err.println("NumberParseException was thrown: " + e.toString());
		}
		
		if (phoneUtil.isValidNumber(numberProto)) {*/
			//Parse.push
			sendCodeTo(number);
		/*} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			AlertDialog dialog = alertDialogBuilder.create();
			dialog.setMessage("Please enter a valid phone number");
			dialog.show();
		}*/
	}

}
