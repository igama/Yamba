package org.igamahq.yamba;


import winterwell.jtwitter.Twitter; 
import winterwell.jtwitter.TwitterException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View; 
import android.view.View.OnClickListener; 
import android.widget.Button; 
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/* public class StatusActivity extends Activity {
    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
    }
}*/

	public class StatusActivity extends Activity implements OnClickListener, TextWatcher {
		private static final String TAG = "StatusActivity"; 
		EditText editText; 
		Button updateButton;
		Twitter twitter;
		TextView textCount;
		SharedPreferences prefs;

		/** Called when the activity is first created. */ 
		@Override 
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState); 
			setContentView(R.layout.status);

			// Find views 
			editText = (EditText) findViewById(R.id.editText); // 
			updateButton = (Button) findViewById(R.id.buttonUpdate);
			updateButton.setOnClickListener(this);
			
			textCount = (TextView) findViewById(R.id.textCount); // 
			textCount.setText(Integer.toString(140)); // 
			textCount.setTextColor(Color.GREEN);
			
			editText.addTextChangedListener(this); //attach TextWatcher to our editText field
		
		}
		
		
		// Called when button is clicked
		public void onClick(View v){

			//Log.d(TAG, "onClicked");
			// Update twitter status 
			try {
				String status = editText.getText().toString(); 
				new PostToTwitter().execute(status); // 
				}
			catch (TwitterException e) {
				Log.d(TAG, "Twitter setStatus failed: " + e);
				}
		}
		
		// Asynchronously posts to twitter 
		class PostToTwitter extends AsyncTask<String, Integer, String> { //
			// Called to initiate the background activity

			@Override
			protected String doInBackground(String... statuses){
				try { 
					YambaApplication yamba = ((YambaApplication) getApplication()); // 
					Twitter.Status status = yamba.getTwitter().updateStatus(statuses[0]); //
					return status.text;
				} catch (TwitterException e) {
					Log.e(TAG, "Failed to connect to twitter service", e); 
					e.printStackTrace();  // To Be Removed
					return "Failed to post";
				}	
			}

			// Called when there's a status to be updated 
			@Override 
			protected void onProgressUpdate(Integer... values) { //
				super.onProgressUpdate(values); 
				// Not used in this case
			}

			// Called once the background activity has completed
			@Override 
			protected void onPostExecute(String result) { //
				Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
			}


		}//end posttotwitter
		
		// TextWatcher Methods
		public void afterTextChanged(Editable statusText){
			int count = 140 - statusText.length();
			textCount.setText(Integer.toString(count));
			textCount.setTextColor(Color.GREEN);
			if (count < 10)
				textCount.setTextColor(Color.YELLOW); 
			if (count < 0)
				textCount.setTextColor(Color.RED);
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) { // 
		
		}
	
		public void onTextChanged(CharSequence s, int start, int before, int count) { // 
		
		}
		
		// Called first time user clicks on the menu button 
		@Override 
		public boolean onCreateOptionsMenu(Menu menu) {
			MenuInflater inflater = getMenuInflater();	// 
			inflater.inflate(R.menu.menu, menu);	// 
			return true; //
		}
		
		// Called when an options item is clicked 
		@Override 
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {	// 
			case R.id.itemPrefs:
				startActivity(new Intent(this, PrefsActivity.class)); // 
				break;
			case R.id.itemServiceStart:
				startService(new Intent(this, UpdaterService.class));
				break;
			case R.id.itemServiceStop:
				stopService(new Intent(this, UpdaterService.class));
				break;
			} 
			return true;
		}
		
		
	} // end StatusActivity

	