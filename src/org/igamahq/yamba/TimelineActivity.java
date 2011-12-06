package org.igamahq.yamba;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.TextView;

public class TimelineActivity extends Activity {
	DbHelper dbHelper;
	SQLiteDatabase db;
	Cursor cursor;
	TextView textTimeline;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.timeline_basic);
		// Find your views 
		textTimeline = (TextView) findViewById(R.id.textTimeline);
		
		// Connect to database 
		dbHelper = new DbHelper(this); // 
		db = dbHelper.getReadableDatabase();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// Get the data from the database 
		cursor = db.query(DbHelper.TABLE, null, null, null, null, null, DbHelper.C_CREATED_AT + " DESC"); // 
		startManagingCursor(cursor);
		
		// Iterate over all the data and print it out 
		String user, text, output; 
		while (cursor.moveToNext()) { //
			user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER)); // Get Username 
			text = cursor.getString(cursor.getColumnIndex(DbHelper.C_TEXT)); // Get Twitte
			output = String.format("%s: %s\n", user, text); // Create output string
			textTimeline.append(output); //append to output view
		}
		
	}
	
} // end class TimelineActivity
