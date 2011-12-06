package org.igamahq.yamba;

import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class UpdaterService extends Service {
	static final String TAG = "UpdaterService";
	
	static final int DELAY = 60000; // a minute 
	private boolean runFlag = false; // know whether the service is currently running.
	private Updater updater;
	
	private YambaApplication yamba; // access to the YambaApplication object that contains our shared features
	
	DbHelper dbHelper; // 
	SQLiteDatabase db;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		this.yamba = (YambaApplication) getApplication();
		this.updater = new Updater(); // Updater is the separate thread that performs the actual network update.
		
		dbHelper = new DbHelper(this);
		
		Log.d(TAG,"onCreate");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		this.runFlag = true; //
		this.updater.start();
		this.yamba.setServiceRunning(true); //  update the serviceRunning flag in the shared application object
		
		Log.d(TAG,"onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.runFlag = false; //
		this.updater.interrupt(); // To stop the actual thread from running, we invoke interrupt() on it.
		this.updater = null; // Set it to null to help the garbage collection process clean it up.
		this.yamba.setServiceRunning(false); 
		
		Log.d(TAG,"onDestroy");
	}


	/** * Thread that performs the actual update from the online service */
	private class Updater extends Thread { //It is a thread, so it extends Java’s Thread class.
		List<Twitter.Status> timeline;
		
		public Updater() { 
			super("UpdaterService-Updater");
		}

		public void run_old() { //
			UpdaterService updaterService = UpdaterService.this;
			while (updaterService.runFlag) { 
				Log.d(TAG, "Updater running");
				try { 
					// Get the timeline from the cloud 
					try {
						timeline = yamba.getTwitter().getFriendsTimeline(); // latest twitts
						} catch (TwitterException e) {
							Log.e(TAG, "Failed to connect to twitter service", e);
						}
					
					// Open the database for writing
					db = dbHelper.getWritableDatabase();
					
					//ContentValues is a simple name-value pair data structure that
					// maps database table names to their respective values.
					ContentValues values = new ContentValues();
					
					// Loop over the timeline and print it out 
					for (Twitter.Status status : timeline) { //
						// Insert into database values.clear(); // 
						values.put(DbHelper.C_ID, status.id); 
						values.put(DbHelper.C_CREATED_AT, status.createdAt.getTime()); 
						values.put(DbHelper.C_SOURCE, status.source); 
						values.put(DbHelper.C_TEXT, status.text); 
						values.put(DbHelper.C_USER, status.user.name); 
						
						try {
							db.insertOrThrow(DbHelper.TABLE, null, values); // prepared statement approach to inserting into the database.
						} catch (SQLException e) {
							// ignore
						}
						
						Log.d(TAG, String.format("%s: %s", status.user.name, status.text)); //
					}
					
					//Close the database 
					//This is important because another activity could
					// be trying to read or write from this shared resource.
					db.close(); 
					
					Log.d(TAG, "Updater ran"); 
					Thread.sleep(DELAY); //
				} catch (InterruptedException e) { // 
					updaterService.runFlag = false;
				}
			}
		}
		
		
	    @Override
	    public void run() {
	      UpdaterService updaterService = UpdaterService.this;
	      while (updaterService.runFlag) {
	        Log.d(TAG, "Running background thread");
	        try {
	          YambaApplication yamba = (YambaApplication) updaterService
	              .getApplication();
	          int newUpdates = yamba.fetchStatusUpdates();
	          if (newUpdates > 0) {
	            Log.d(TAG, "We have a new status");
	          }
	          Thread.sleep(DELAY);
	        } catch (InterruptedException e) {
	          updaterService.runFlag = false;
	        }
	      }
	    }

		
		
	} // end Updater thread
	
} // end UpdaterService
